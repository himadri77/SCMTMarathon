package net.himadri.scmt.client.panel;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.MarathonServiceAsync;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.Utils;
import net.himadri.scmt.client.callback.EmptyFailureHandlingAsyncCallback;
import net.himadri.scmt.client.dialog.CorrectionDialogBox;
import net.himadri.scmt.client.gwtextras.ImageButton;
import net.himadri.scmt.client.gwtextras.SafeHtmlColumn;
import net.himadri.scmt.client.gwtextras.SafeHtmlString;
import net.himadri.scmt.client.serializable.RaceStatusRow;

import java.text.ParseException;
import java.util.List;

public class ProblemsPanel extends Composite {
    private SCMTMarathon scmtMarathon;

    private MarathonServiceAsync marathonService = GWT.create(MarathonService.class);
    private ListDataProvider<StatusTableType> problemsList = new ListDataProvider<StatusTableType>();
    private IntegerBox elteresBox = new IntegerBox();
    private CorrectionDialogBox correctionDialogBox;

    private static class StatusTableType {
        public enum Type {BIG_LAP_DIFFERENCE, SMALL_LAP_DIFFERENCE, EXTRA_LAP, UNKNOWN_NUMBER}
        Type type;
        RaceStatusRow raceStatusRow;
        String description;
        int problemLapIndex;

        private StatusTableType(Type type, RaceStatusRow raceStatusRow, String description, int problemLapIndex) {
            this.type = type;
            this.raceStatusRow = raceStatusRow;
            this.description = description;
            this.problemLapIndex = problemLapIndex;
        }
    }

    public ProblemsPanel(final SCMTMarathon scmtMarathon) {
        this.scmtMarathon = scmtMarathon;
        CellTable<StatusTableType> statusTable = new CellTable<StatusTableType>();
        ScrollPanel statusScrollPanel = new ScrollPanel();
        statusScrollPanel.setSize("100%", "370px");
        statusScrollPanel.setWidget(statusTable);
        statusTable.setSize("100%", "100%");
        statusTable.addColumn(new TextColumn<StatusTableType>() {
            @Override
            public String getValue(StatusTableType statusTableType) {
                return statusTableType.raceStatusRow.getRaceNumber();
            }
        }, "Rajtszám");
        statusTable.addColumn(new TextColumn<StatusTableType>() {
            @Override
            public String getValue(StatusTableType statusTableType) {
                return statusTableType.raceStatusRow.getVersenyzo() != null ? statusTableType.raceStatusRow.getVersenyzo().getName() : null;
            }
        }, "Versenyző");
        statusTable.addColumn(new TextColumn<StatusTableType>() {
            @Override
            public String getValue(StatusTableType statusTableType) {
                return statusTableType.raceStatusRow.getVersenySzam() != null ? Utils.getVersenySzamMegnevezes(scmtMarathon, statusTableType.raceStatusRow.getVersenySzam()) : null;
            }
        }, "Versenyszám");
        statusTable.addColumn(new SafeHtmlColumn<StatusTableType>() {
            @Override
            public SafeHtml getValue(StatusTableType statusTableType) {
                StringBuilder stringBuilder = new StringBuilder();
                List<Long> lapTimes = statusTableType.raceStatusRow.getLapTimes();
                if (statusTableType.problemLapIndex == 0) stringBuilder.append("<b>");
                stringBuilder.append("1. kör: ").append(Utils.getElapsedTimeString(statusTableType.raceStatusRow, 0));
                if (statusTableType.problemLapIndex == 0) stringBuilder.append("</b>");
                for (int i = 0; i < lapTimes.size() - 1; i++) {
                    if (statusTableType.problemLapIndex == i + 1) stringBuilder.append("<b>");
                    stringBuilder.append(", ").append(i + 2).append(" kör: ");
                    stringBuilder.append(Utils.getElapsedTimeString(lapTimes.get(i + 1) - lapTimes.get(i)));
                    if (statusTableType.problemLapIndex == i + 1) stringBuilder.append("</b>");
                }
                return new SafeHtmlString(stringBuilder.toString());
            }
        }, "Köridők");
        statusTable.addColumn(new TextColumn<StatusTableType>() {
            @Override
            public String getValue(StatusTableType statusTableType) {
                return statusTableType.description;
            }
        }, "Probléma");
        statusTable.addColumn(new MegoldasColumn(), "Megoldás");
        problemsList.addDataDisplay(statusTable);
        statusTable.setPageSize(Integer.MAX_VALUE);

        HorizontalPanel elteresHorizontal = new HorizontalPanel();
        elteresHorizontal.setSpacing(10);
        elteresHorizontal.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        elteresHorizontal.add(new Label("Eltérés százalékban: "));
        elteresBox.setVisibleLength(3);
        elteresBox.setMaxLength(3);
        elteresBox.setValue(150);
        elteresHorizontal.add(elteresBox);
        elteresHorizontal.add(new ImageButton("viewmag.png", "Keresés", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                findAllProblems();
            }
        }));

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setSize("100%", "620px");
        mainPanel.setSpacing(10);
        mainPanel.add(statusScrollPanel);
        mainPanel.add(elteresHorizontal);

        initWidget(mainPanel);
    }

    private void findAllProblems() {
        problemsList.getList().clear();
        findBigLapTimeDifference();
        findSmallLapTimeDifference();
        findExtraLaps();
        findUnknownNumbers();
    }

    private void findBigLapTimeDifference() {
        try {
            int elteres = elteresBox.getValueOrThrow();
            List<RaceStatusRow> raceStatusRows = scmtMarathon.getRaceStatusRowCache ().getAllRaceStatusRows();
            for (RaceStatusRow raceStatusRow: raceStatusRows) {
                List<Long> lapTimes = raceStatusRow.getLapTimes();
                if (lapTimes.size() >= 2) {
                    IndexAndValue maxDiff = findMaximumDiff(lapTimes);
                    long sumDiff = getSumDiff(lapTimes);
                    double avgDiffWithoutMax = (sumDiff - maxDiff.value) / (lapTimes.size() - 1);
                    double avgDiffThreshold = avgDiffWithoutMax * (elteres / 100.0);
                    if (maxDiff.value >= avgDiffThreshold) {
                        problemsList.getList().add(new StatusTableType(StatusTableType.Type.BIG_LAP_DIFFERENCE, raceStatusRow, "Túl lassú kör", maxDiff.index));
                    }
                }
            }
        } catch (ParseException e) {
            Window.alert("Nem értelmezhető szám az eltérésnél.");
        }
    }

    private void findSmallLapTimeDifference() {
        try {
            int elteres = elteresBox.getValueOrThrow() - 100;
            List<RaceStatusRow> raceStatusRows = scmtMarathon.getRaceStatusRowCache ().getAllRaceStatusRows();
            for (RaceStatusRow raceStatusRow: raceStatusRows) {
                List<Long> lapTimes = raceStatusRow.getLapTimes();
                if (lapTimes.size() >= 2) {
                    IndexAndValue minDiff = findMinimumDiff(lapTimes);
                    long sumDiff = getSumDiff(lapTimes);
                    double avgDiffWithoutMin = (sumDiff - minDiff.value) / (lapTimes.size() - 1);
                    double avgDiffThreshold = avgDiffWithoutMin * (elteres / 100.0);
                    if (minDiff.value <= avgDiffThreshold) {
                        problemsList.getList().add(new StatusTableType(StatusTableType.Type.SMALL_LAP_DIFFERENCE, raceStatusRow, "Túl gyors kör", minDiff.index));
                    }
                }
            }
        } catch (ParseException e) {
            Window.alert("Nem értelmezhető szám az eltérésnél.");
        }
    }

    private IndexAndValue findMaximumDiff(List<Long> longList) {
        long maxValue = longList.get(0);
        int index = 0;
        for (int i = 0; i < longList.size() - 1; i++) {
            long diff = longList.get(i + 1) - longList.get(i);
            if (diff > maxValue) {
                index = i + 1;
                maxValue = diff;
            }
        }
        return new IndexAndValue(index, maxValue);
    }

    private IndexAndValue findMinimumDiff(List<Long> longList) {
        long minValue = longList.get(0);
        int index = 0;
        for (int i = 0; i < longList.size() - 1; i++) {
            long diff = longList.get(i + 1) - longList.get(i);
            if (diff < minValue) {
                index = i + 1;
                minValue = diff;
            }
        }
        return new IndexAndValue(index, minValue);
    }

    private long getSumDiff(List<Long> longList) {
        long sum = longList.get(0);
        for (int i = 0; i < longList.size() - 1; i++) {
            sum += longList.get(i + 1) - longList.get(i);
        }
        return sum;
    }

    private void findExtraLaps() {
        List<RaceStatusRow> raceStatusRows = scmtMarathon.getRaceStatusRowCache ().getAllRaceStatusRows();
        for (RaceStatusRow raceStatusRow: raceStatusRows) {
            if (raceStatusRow.getTav() != null && raceStatusRow.getLapTimes().size() > raceStatusRow.getTav().getKorSzam()) {
                problemsList.getList().add(new StatusTableType(StatusTableType.Type.EXTRA_LAP, raceStatusRow, "Extra kör.", raceStatusRow.getTav().getKorSzam()));
            }
        }
    }

    private void findUnknownNumbers() {
        List<RaceStatusRow> raceStatusRows = scmtMarathon.getRaceStatusRowCache ().getAllRaceStatusRows();
        for (RaceStatusRow raceStatusRow: raceStatusRows) {
            if (raceStatusRow.getVersenyzo() == null) {
                problemsList.getList().add(new StatusTableType(StatusTableType.Type.UNKNOWN_NUMBER, raceStatusRow, "Ismeretlen rajtszám", 0));
            }
        }
    }

    private class MegoldasColumn extends Column<StatusTableType, String> {
        public MegoldasColumn() {
            super(new ButtonCell());
            setHorizontalAlignment(ALIGN_CENTER);
            setFieldUpdater(new FieldUpdater<StatusTableType, String>() {
                @Override
                public void update(int i, StatusTableType statusTableType, String s) {
                    switch (statusTableType.type) {
                        case BIG_LAP_DIFFERENCE:
                            addExtraKor(statusTableType.raceStatusRow);
                            break;
                        case SMALL_LAP_DIFFERENCE:
                        case EXTRA_LAP:
                        case UNKNOWN_NUMBER:
                            showCorrectionDialogBox(statusTableType.raceStatusRow.getRaceNumber());
                    }
                }
            });
        }

        private void addExtraKor(RaceStatusRow raceStatusRow) {
            String korido = Window.prompt("Add meg az extra köridőt", "");
            try {
                long parseTime = Utils.parseTime(korido.trim()) + Utils.getFutamTimeDiff(scmtMarathon, raceStatusRow.getRaceNumber());
                marathonService.addPersonLap(scmtMarathon.getVerseny().getId(), raceStatusRow.getRaceNumber(), parseTime, false, new EmptyFailureHandlingAsyncCallback<Void>());
            } catch (ParseException e) {
                SCMTMarathon.commonFailureHandling(e);
            }
        }

        private void showCorrectionDialogBox(String raceNumber) {
            if (correctionDialogBox == null) {
                correctionDialogBox = new CorrectionDialogBox(scmtMarathon);
            }
            correctionDialogBox.showDialog(raceNumber);
        }

        @Override
        public String getValue(StatusTableType statusTableType) {
            switch (statusTableType.type) {
                case BIG_LAP_DIFFERENCE:
                    return "Extra kör hozzáadása";
                case SMALL_LAP_DIFFERENCE:
                    return "Kör módosítása";
                case EXTRA_LAP:
                    return "Kör törlése";
                case UNKNOWN_NUMBER:
                    return "Felvitel törlése";
                default: return null;
            }
        }
    }

    private static class IndexAndValue {
        int index;
        long value;

        public IndexAndValue(int index, long value) {
            this.index = index;
            this.value = value;
        }
    }
}
