package io.opengemini.client.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class QueryResult {
    private List<SeriesResult> results;

    private String error;


    public static class SeriesResult {
        private List<Series> series;
        private String error;

        /**
         * @return the series
         */
        public List<Series> getSeries() {
            return this.series;
        }

        /**
         * @param series the series to set
         */
        public void setSeries(final List<Series> series) {
            this.series = series;
        }

        /**
         * Checks if this Result has an error message.
         *
         * @return <code>true</code> if there is an error message,
         * <code>false</code> if not.
         */
        public boolean hasError() {
            return this.error != null;
        }

        /**
         * @return the error
         */
        public String getError() {
            return this.error;
        }

        /**
         * @param error the error to set
         */
        public void setError(final String error) {
            this.error = error;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Result [series=");
            builder.append(this.series);
            builder.append(", error=");
            builder.append(this.error);
            builder.append("]");
            return builder.toString();
        }

    }
}
