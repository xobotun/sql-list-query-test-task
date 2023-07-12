/*
 * This file is generated by jOOQ.
 */
package com.xobotun.jooq.public_.tables.records;


import com.xobotun.jooq.public_.tables.ProductRecommendation;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ProductRecommendationRecord extends UpdatableRecordImpl<ProductRecommendationRecord> implements Record3<Integer, Integer, Integer> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.product_recommendation.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.product_recommendation.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>public.product_recommendation.product_id1</code>.
     */
    public void setProductId1(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.product_recommendation.product_id1</code>.
     */
    public Integer getProductId1() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>public.product_recommendation.product_id2</code>.
     */
    public void setProductId2(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.product_recommendation.product_id2</code>.
     */
    public Integer getProductId2() {
        return (Integer) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<Integer, Integer, Integer> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<Integer, Integer, Integer> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return ProductRecommendation.PRODUCT_RECOMMENDATION.ID;
    }

    @Override
    public Field<Integer> field2() {
        return ProductRecommendation.PRODUCT_RECOMMENDATION.PRODUCT_ID1;
    }

    @Override
    public Field<Integer> field3() {
        return ProductRecommendation.PRODUCT_RECOMMENDATION.PRODUCT_ID2;
    }

    @Override
    public Integer component1() {
        return getId();
    }

    @Override
    public Integer component2() {
        return getProductId1();
    }

    @Override
    public Integer component3() {
        return getProductId2();
    }

    @Override
    public Integer value1() {
        return getId();
    }

    @Override
    public Integer value2() {
        return getProductId1();
    }

    @Override
    public Integer value3() {
        return getProductId2();
    }

    @Override
    public ProductRecommendationRecord value1(Integer value) {
        setId(value);
        return this;
    }

    @Override
    public ProductRecommendationRecord value2(Integer value) {
        setProductId1(value);
        return this;
    }

    @Override
    public ProductRecommendationRecord value3(Integer value) {
        setProductId2(value);
        return this;
    }

    @Override
    public ProductRecommendationRecord values(Integer value1, Integer value2, Integer value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ProductRecommendationRecord
     */
    public ProductRecommendationRecord() {
        super(ProductRecommendation.PRODUCT_RECOMMENDATION);
    }

    /**
     * Create a detached, initialised ProductRecommendationRecord
     */
    public ProductRecommendationRecord(Integer id, Integer productId1, Integer productId2) {
        super(ProductRecommendation.PRODUCT_RECOMMENDATION);

        setId(id);
        setProductId1(productId1);
        setProductId2(productId2);
        resetChangedOnNotNull();
    }
}
