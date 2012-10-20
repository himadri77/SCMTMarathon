package net.himadri.scmt.client.panel;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import net.himadri.scmt.client.EmptyFailureHandlingAsyncCallback;
import net.himadri.scmt.client.MarathonService;
import net.himadri.scmt.client.MarathonServiceAsync;
import net.himadri.scmt.client.SCMTMarathon;
import net.himadri.scmt.client.entity.PageProfile;
import net.himadri.scmt.client.entity.PageProfileId;

import java.util.Arrays;
import java.util.List;

public class NyomtatoPanel extends Composite {

    private MarathonServiceAsync marathonService = GWT.create(MarathonService.class);

	public NyomtatoPanel() {
		
		AbsolutePanel absolutePanel = new AbsolutePanel();
		initWidget(absolutePanel);
		absolutePanel.setSize("900px", "330px");
		
		Label lblEmlklapnyomtatsBelltsai = new Label("Emléklapnyomtatás beállításai");
		absolutePanel.add(lblEmlklapnyomtatsBelltsai, 20, 10);
		
		final CellTable<PageProfile> cellTable = new CellTable<PageProfile>();
        final ListDataProvider<PageProfile> cellTableData = new ListDataProvider<PageProfile>();
        cellTableData.addDataDisplay(cellTable);

        marathonService.getAllPageProfiles(new AsyncCallback<List<PageProfile>>() {
            @Override
            public void onFailure(Throwable throwable) {
                SCMTMarathon.commonFailureHandling(throwable);
            }

            @Override
            public void onSuccess(List<PageProfile> pageProfiles) {
                cellTableData.setList(pageProfiles);
            }
        });

        absolutePanel.add(cellTable, 20, 44);
		cellTable.setSize("459px", "214px");
        cellTable.addColumn(new TextColumn<PageProfile>() {
            @Override
            public String getValue(PageProfile pageProfile) {
                return PageProfileId.valueOf(pageProfile.getId()).getMegnevezes();
            }
        }, "Mező");

        final TextInputCell xTextInputCell = new TextInputCell();
        final Column<PageProfile, String> xColumn = new Column<PageProfile, String>(xTextInputCell) {
            @Override
            public String getValue(PageProfile pageProfile) {
                return Float.toString(pageProfile.getxAxis());
            }
        };
        xColumn.setFieldUpdater(new FieldUpdater<PageProfile, String>() {
            @Override
            public void update(int i, PageProfile pageProfile, String s) {
                try {
                    float xAxis = Float.parseFloat(s);
                    if (xAxis < 0) throw new NumberFormatException();
                    pageProfile.setxAxis(xAxis);
                    saveProfile(pageProfile);
                } catch (NumberFormatException e) {
                    xTextInputCell.clearViewData(pageProfile);
                    cellTable.redraw();
                }
            }
        });
        xColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        cellTable.addColumn(xColumn, "Bal behúzás (cm)");

        final TextInputCell yTextInputCell = new TextInputCell();
        final Column<PageProfile, String> yColumn = new Column<PageProfile, String>(yTextInputCell) {
            @Override
            public String getValue(PageProfile pageProfile) {
                return Float.toString(pageProfile.getyAxis());
            }
        };
        yColumn.setFieldUpdater(new FieldUpdater<PageProfile, String>() {
            @Override
            public void update(int i, PageProfile pageProfile, String s) {
                try {
                    float yAxis = Float.parseFloat(s);
                    if (yAxis < 0) throw new NumberFormatException();
                    pageProfile.setyAxis(yAxis);
                    saveProfile(pageProfile);
                } catch (NumberFormatException e) {
                    yTextInputCell.clearViewData(pageProfile);
                    cellTable.redraw();
                }
            }
        });
        cellTable.addColumn(yColumn, "Felső behúzás (cm)");

        final List<String> fontFamilyOptions = Arrays.asList("COURIER", "HELVETICA", "TIMES_ROMAN");
        SelectionCell fontFamilyCell = new SelectionCell(fontFamilyOptions);
        final Column<PageProfile, String> fontFamilyColumn = new Column<PageProfile, String>(fontFamilyCell) {
            @Override
            public String getValue(PageProfile pageProfile) {
                return pageProfile.getFontFamily();
            }
        };
        fontFamilyColumn.setFieldUpdater(new FieldUpdater<PageProfile, String>() {
            @Override
            public void update(int i, PageProfile pageProfile, String s) {
                pageProfile.setFontFamily(s);
                saveProfile(pageProfile);
            }
        });
        cellTable.addColumn(fontFamilyColumn, "Betűtípus");

        final TextInputCell sizeInputCell = new TextInputCell();
        final Column<PageProfile, String> sizeColumn = new Column<PageProfile, String>(sizeInputCell) {
            @Override
            public String getValue(PageProfile pageProfile) {
                return Integer.toString(pageProfile.getSize());
            }
        };
        sizeColumn.setFieldUpdater(new FieldUpdater<PageProfile, String>() {
            @Override
            public void update(int i, PageProfile pageProfile, String s) {
                try {
                    int size = Integer.parseInt(s);
                    if (size < 0) throw new NumberFormatException();
                    pageProfile.setSize(size);
                    saveProfile(pageProfile);
                } catch (NumberFormatException e) {
                    sizeInputCell.clearViewData(pageProfile);
                    cellTable.redraw();
                }
            }
        });
        cellTable.addColumn(sizeColumn, "Betűméret");

        final List<String> alignmentOptions = Arrays.asList("Balra igazítás", "Középre igazítás", "Jobbra igazítás");
        SelectionCell alignmentCell = new SelectionCell(alignmentOptions);
        final Column<PageProfile, String> igazitasColumn = new Column<PageProfile, String>(alignmentCell) {
            @Override
            public String getValue(PageProfile pageProfile) {
                return alignmentOptions.get(pageProfile.getAlignment());
            }
        };
        igazitasColumn.setFieldUpdater(new FieldUpdater<PageProfile, String>() {
            @Override
            public void update(int i, PageProfile pageProfile, String s) {
                pageProfile.setAlignment(alignmentOptions.indexOf(s));
                saveProfile(pageProfile);
            }
        });
        cellTable.addColumn(igazitasColumn, "Igazítás");
		
        final FormPanel formPanel = new FormPanel();
        final Hidden pdfServiceParam = new Hidden("versenySzam");
        formPanel.setAction("/scmtmarathon/PDFService");
        formPanel.setMethod("GET");
        formPanel.add(pdfServiceParam);
        absolutePanel.add(formPanel);

        Button probaNyomtatas = new Button("Próbanyomtatás", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                pdfServiceParam.setValue("minta");
                formPanel.submit();
            }
        });
        absolutePanel.add(probaNyomtatas, 20, 282);
		probaNyomtatas.setSize("150px", "30px");
        cellTable.setColumnWidth(cellTable.getColumn(2), 50, Style.Unit.PX);
    }

    private void saveProfile(PageProfile pageProfile) {
        marathonService.savePageProfile(pageProfile, new EmptyFailureHandlingAsyncCallback<Void>());
    }
}
