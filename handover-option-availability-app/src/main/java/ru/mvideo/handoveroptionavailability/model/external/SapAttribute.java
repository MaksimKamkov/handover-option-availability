package ru.mvideo.handoveroptionavailability.model.external;

public interface SapAttribute {

	/**
	 * Единицы измерения габаритов.
	 */
	String DIMENSION_MEASURE = "m_dimensionsMeasure";

	/**
	 * Габариты.
	 */
	String WIDTH = "m_width";
	String HEIGHT = "m_height";
	String LENGTH = "m_length";

	/**
	 * Единицы измерения веса.
	 */
	String WEIGHT_MEASURE = "m_weightMeasure";
	/**
	 * Вес.
	 */
	String WEIGHT = "m_weight";
}