package net.himadri.scmt.client.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Kavai
 * Date: 2012.04.22. 5:51
 */
@Entity
public class Verseny implements Serializable {
    @SuppressWarnings({"UnusedDeclaration"})
    @Id
    private Long id;
    private String nev;
    private Long raceStartTime;
    private RaceStatus raceStatus = RaceStatus.NOT_STARTED;
    private int personLapSyncValue = 1;
    private int versenyzoSyncValue = 1;
    private int versenySzamSyncValue = 1;
    private int tavSyncValue = 1;

    @SuppressWarnings({"UnusedDeclaration"})
    public Verseny() {
    }

    public Verseny(String nev) {
        this.nev = nev;
    }

    public Long getId() {
        return id;
    }

    public String getNev() {
        return nev;
    }

    public void setNev(String nev) {
        this.nev = nev;
    }

    public Long getRaceStartTime() {
        return raceStartTime;
    }

    public void setRaceStartTime(Long raceStartTime) {
        this.raceStartTime = raceStartTime;
    }

    public RaceStatus getRaceStatus() {
        return raceStatus;
    }

    public void setRaceStatus(RaceStatus raceStatus) {
        this.raceStatus = raceStatus;
    }

    public int getPersonLapSyncValue() {
        return personLapSyncValue;
    }

    public int getVersenyzoSyncValue() {
        return versenyzoSyncValue;
    }

    public int getVersenySzamSyncValue() {
        return versenySzamSyncValue;
    }

    public int getTavSyncValue() {
        return tavSyncValue;
    }

    public void setPersonLapSyncValue(int personLapSyncValue) {
        this.personLapSyncValue = personLapSyncValue;
    }

    public void setVersenyzoSyncValue(int versenyzoSyncValue) {
        this.versenyzoSyncValue = versenyzoSyncValue;
    }

    public void setVersenySzamSyncValue(int versenySzamSyncValue) {
        this.versenySzamSyncValue = versenySzamSyncValue;
    }

    public void setTavSyncValue(int tavSyncValue) {
        this.tavSyncValue = tavSyncValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Verseny verseny = (Verseny) o;

        return (id != null ? id.equals(verseny.id) : verseny.id == null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}