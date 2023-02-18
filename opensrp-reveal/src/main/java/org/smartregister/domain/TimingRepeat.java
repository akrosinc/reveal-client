package org.smartregister.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.sql.Time;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class TimingRepeat implements Serializable {

	private int count;
	private int countMax;
	private float duration;
	private float durationMax;
	private DurationCode code;
	private int frequency;
	private int frequencyMax;
	private float period;
	private float periodMax;
	private DurationCode periodUnit;
	private List<DayOfWeek> dayOfWeek;
	private List<Time> timeOfDay;
	private List<String> when;
	private int offset;

	public enum DurationCode {
		s, min, h, d, wk, mo, a;
	}

	public enum DayOfWeek {
		mon, tue, wed, thu, fri, sat, sun
	}

}
