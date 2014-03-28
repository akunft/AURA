package de.tuberlin.aura.core.statistic;

/**
 * Created by teots on 3/12/14.
 */
public class TimeMeasurement extends Measurement {

    private static final boolean MEASURE_TIME_IN_MILLISECONDS = true;

    private long time = 0;

    public TimeMeasurement(MeasurementType type, String description, long time) {
        super(type, description);
        this.time = time;
    }

    @Override
    public String getHeader() {
        return "TIMESTAMP\tDESCRIPTION\tTIME";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (!(obj instanceof TimeMeasurement))
            return false;

        TimeMeasurement other = (TimeMeasurement) obj;
        if (this.timestamp == other.timestamp && this.type == other.type && this.description.equals(other.description) && this.time == other.time)
            return true;

        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.timestamp);
        result = prime * result + (this.type.hashCode());
        result = prime * result + (this.description.hashCode());
        result = prime * result + (int) (this.time);

        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getTimestamp());
        builder.append("\t");
        builder.append(this.description);
        builder.append("\t");
        builder.append(this.time);

        return builder.toString();
    }
}
