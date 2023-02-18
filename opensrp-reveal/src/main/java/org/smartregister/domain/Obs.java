package org.smartregister.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Obs implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@JsonProperty
	private String fieldType;
	
	@JsonProperty
	private String fieldDataType;
	
	@JsonProperty
	private String fieldCode;
	
	@JsonProperty
	private String parentCode;
	
	@JsonProperty
	private List<Object> values;
	
	//hold unique values here
	Set<String> set = new HashSet<String>();
	
	@JsonProperty
	private String comments;
	
	@JsonProperty
	private String formSubmissionField;
	
	@JsonProperty
	private DateTime effectiveDatetime;
	
	@JsonProperty
	private List<Object> humanReadableValues;

	@JsonProperty
	private Map<String, Object> keyValPairs;

	private boolean saveObsAsArray;
	
	public Obs() {
	}
	
	public Obs(String fieldType, String fieldDataType, String fieldCode, String parentCode, List<Object> values,
	    List<Object> humanReadableValues, String comments, String formSubmissionField) {
		this.setFieldType(fieldType);
		this.fieldDataType = fieldDataType;
		this.fieldCode = fieldCode;
		this.parentCode = parentCode;
		this.values = values;
		this.humanReadableValues = humanReadableValues;
		this.comments = comments;
		this.formSubmissionField = formSubmissionField;
	}
	
	public Obs(String fieldType, String fieldDataType, String fieldCode, String parentCode, Object value, String comments,
	    String formSubmissionField) {
		this.setFieldType(fieldType);
		this.fieldDataType = fieldDataType;
		this.fieldCode = fieldCode;
		this.parentCode = parentCode;
		addToValueList(value);
		this.comments = comments;
		this.formSubmissionField = formSubmissionField;
	}
	
	public Obs(String fieldType, String fieldDataType, String fieldCode, String parentCode, List<Object> values,
	    String comments, String formSubmissionField, List<Object> humanReadableValues) {
		this.setFieldType(fieldType);
		this.fieldDataType = fieldDataType;
		this.fieldCode = fieldCode;
		this.parentCode = parentCode;
		this.values = values;
		this.comments = comments;
		this.formSubmissionField = formSubmissionField;
		this.humanReadableValues = humanReadableValues;
	}
	
	public Obs(String fieldType, String fieldDataType, String fieldCode, String parentCode, List<Object> values,
	    String comments, String formSubmissionField, List<Object> humanReadableValues, boolean saveObsAsArray) {
		this(fieldType, fieldDataType, fieldCode, parentCode, values, comments, formSubmissionField, humanReadableValues);
		this.saveObsAsArray = saveObsAsArray;
	}

	public Obs(String fieldType, String fieldDataType, String fieldCode, String parentCode,
			   Map<String, Object> keyValPairs, List<Object> values, List<Object> humanReadableValues,
			   String comments, String formSubmissionField, boolean saveObsAsArray) {
		this(fieldType, fieldDataType, fieldCode, parentCode, values, comments, formSubmissionField, humanReadableValues, saveObsAsArray);
		setKeyValPairs(keyValPairs);
	}
	
	public String getFieldType() {
		return fieldType;
	}
	
	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}
	
	public String getFieldDataType() {
		return fieldDataType;
	}
	
	public void setFieldDataType(String fieldDataType) {
		this.fieldDataType = fieldDataType;
	}
	
	public String getFieldCode() {
		return fieldCode;
	}
	
	public void setFieldCode(String fieldCode) {
		this.fieldCode = fieldCode;
	}
	
	public String getParentCode() {
		return parentCode;
	}
	
	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}
	
	@JsonIgnore
	public Object getValue() {
		
		if (values == null || values.size() == 0) {
			return null;
		}
		
		if (values.size() > 1) {
			throw new RuntimeException(
			        "Multiset values can not be handled like single valued fields. Use function getValues");
		}
		
		return values.get(0);
	}
	
	@JsonIgnore
	public void setValue(Object value) {
		addToValueList(value);
	}
	
	public List<Object> getHumanReadableValues() {
		return humanReadableValues;
	}
	
	public void setHumanReadableValues(List<Object> humanReadableValues) {
		this.humanReadableValues = humanReadableValues;
	}
	
	public List<Object> getValues() {
		return values;
	}
	
	public void setValues(List<Object> values) {
		this.values = values;
	}
	
	public String getComments() {
		return comments;
	}
	
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public String getFormSubmissionField() {
		return formSubmissionField;
	}
	
	public void setFormSubmissionField(String formSubmissionField) {
		this.formSubmissionField = formSubmissionField;
	}
	
	public DateTime getEffectiveDatetime() {
		return effectiveDatetime;
	}
	
	public void setEffectiveDatetime(DateTime effectiveDatetime) {
		this.effectiveDatetime = effectiveDatetime;
	}
	
	public boolean isSaveObsAsArray() {
		return saveObsAsArray;
	}
	
	public void setSaveObsAsArray(boolean saveObsAsArray) {
		this.saveObsAsArray = saveObsAsArray;
	}
	
	public Obs withFieldType(String fieldType) {
		this.fieldType = fieldType;
		return this;
	}
	
	public Obs withFieldDataType(String fieldDataType) {
		this.fieldDataType = fieldDataType;
		return this;
	}
	
	public Obs withFieldCode(String fieldCode) {
		this.fieldCode = fieldCode;
		return this;
	}
	
	public Obs withParentCode(String parentCode) {
		this.parentCode = parentCode;
		return this;
	}
	
	public Obs withValue(Object value) {
		return addToValueList(value);
	}
	
	public Obs withValues(List<Object> values) {
		this.values = values;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public Obs addToValueList(Object value) {
		if (value == null) {
			return this;
		}
		if (values == null) {
			values = new ArrayList<>();
		}
		//make the object a bit more linear. If value is a list, the values json object will be an array with arrays inside
		
		if (value instanceof List) {
			set.addAll((Collection<String>) value);
			
		} else {
			set.add(value.toString());
		}
		values.clear();
		values.addAll(set);
		return this;
	}
	
	public Obs withComments(String comments) {
		this.comments = comments;
		return this;
	}
	
	public Obs withFormSubmissionField(String formSubmissionField) {
		this.formSubmissionField = formSubmissionField;
		return this;
	}
	
	public Obs withEffectiveDatetime(DateTime effectiveDatetime) {
		this.effectiveDatetime = effectiveDatetime;
		return this;
		
	}
	
	public Obs withHumanReadableValues(List<Object> humanReadableValues) {
		this.humanReadableValues = humanReadableValues;
		return this;
	}
	
	public Obs addToHumanReadableValuesList(Object humanReadableValue) {
		if (humanReadableValues == null) {
			humanReadableValues = new ArrayList<Object>();
		}
		humanReadableValues.add(humanReadableValue);
		return this;
	}
	
	public Obs withHumanReadableValue(Object humanReadableValue) {
		return addToHumanReadableValuesList(humanReadableValue);
	}
	
	public Obs withsaveObsAsArray(boolean saveObsAsArray) {
		setSaveObsAsArray(saveObsAsArray);
		return this;
	}

	@JsonIgnore
	public Object getHumanReadableValue() {
		if (humanReadableValues.size() > 1) {
			throw new RuntimeException(
			        "Multiset values can not be handled like single valued fields. Use function getValues");
		}
		if (humanReadableValues == null || humanReadableValues.size() == 0) {
			return null;
		}
		return humanReadableValues.get(0);
	}

	public void setHumanReadableValue(Object humanReadableValue) {
		addToHumanReadableValuesList(humanReadableValue);
	}
	
	@Override
	public final boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o, "set");
	}
	
	@Override
	public final int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "set");
	}
	
	@Override
	public String toString() {
		return new GsonBuilder().setPrettyPrinting().create().toJson(this);
	}


	public Set<String> getSet() {
		return set;
	}

	public void setSet(Set<String> set) {
		this.set = set;
	}

	public Map<String, Object> getKeyValPairs() {
		return keyValPairs;
	}

	public void setKeyValPairs(Map<String, Object> keyValPairs) {
		this.keyValPairs = keyValPairs;
	}

	public Obs withKeyValPairs(Map<String, Object> keyValPairs) {
		setKeyValPairs(keyValPairs);
		return this;
	}
}
