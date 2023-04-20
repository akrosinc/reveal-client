package org.smartregister.domain;

import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.AbstractMap;

/**
 * Created by samuelgithengi on 4/29/19.
 */
public class Target implements Serializable {

	private static final long serialVersionUID = 1L;

    private String measure;

    private Detail detail;

    private LocalDate due;

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public LocalDate getDue() {
        return due;
    }

    public void setDue(LocalDate due) {
        this.due = due;
    }

    public Detail getDetail() {
        return detail;
    }

    public void setDetail(Detail detail) {
        this.detail = detail;
    }

    static class Detail implements Serializable {
        private Measure detailQuantity;

        private MeasureRange detailRange;

        private AbstractMap.SimpleEntry<String, String> detailCodableConcept;

        public Measure getDetailQuantity() {
            return detailQuantity;
        }

        public void setDetailQuantity(Measure detailQuantity) {
            this.detailQuantity = detailQuantity;
        }

        public MeasureRange getDetailRange() {
            return detailRange;
        }

        public void setDetailRange(MeasureRange detailRange) {
            this.detailRange = detailRange;
        }

        public AbstractMap.SimpleEntry<String, String> getDetailCodableConcept() {
            return detailCodableConcept;
        }

        public void setDetailCodableConcept(AbstractMap.SimpleEntry<String, String> detailCodableConcept) {
            this.detailCodableConcept = detailCodableConcept;
        }
    }

    static class Measure implements Serializable {
        private float value;

        private String comparator;

        private String unit;

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }

        public String getComparator() {
            return comparator;
        }

        public void setComparator(String comparator) {
            this.comparator = comparator;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }

    static class MeasureRange implements Serializable {
        private Measure high;

        private Measure low;

        public Measure getHigh() {
            return high;
        }

        public void setHigh(Measure high) {
            this.high = high;
        }

        public Measure getLow() {
            return low;
        }

        public void setLow(Measure low) {
            this.low = low;
        }
    }

    static class DetailCodableConcept {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}


