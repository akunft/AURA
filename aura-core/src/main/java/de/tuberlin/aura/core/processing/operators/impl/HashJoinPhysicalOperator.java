package de.tuberlin.aura.core.processing.operators.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.reflectasm.FieldAccess;

import de.tuberlin.aura.core.common.utils.IVisitor;
import de.tuberlin.aura.core.processing.operators.base.AbstractBinaryPhysicalOperator;
import de.tuberlin.aura.core.processing.operators.base.IOperatorEnvironment;
import de.tuberlin.aura.core.processing.operators.base.IPhysicalOperator;
import de.tuberlin.aura.core.record.RowRecordModel;
import de.tuberlin.aura.core.record.TypeInformation;
import de.tuberlin.aura.core.record.tuples.Tuple2;

/**
 *
 */
public final class HashJoinPhysicalOperator<I1,I2> extends AbstractBinaryPhysicalOperator<I1,I2,Tuple2<I1,I2>> {

    // ---------------------------------------------------
    // Fields.
    // ---------------------------------------------------

    private final TypeInformation input1TypeInfo;

    private final TypeInformation input2TypeInfo;

    private final Map<List<Integer>,I1> buildSide;

    // ---------------------------------------------------
    // Constructor.
    // ---------------------------------------------------

    /**
     * Constructor.
     * @param inputOp1
     * @param inputOp2
     */
    public HashJoinPhysicalOperator(final IOperatorEnvironment environment,
                                    final IPhysicalOperator<I1> inputOp1,
                                    final IPhysicalOperator<I2> inputOp2) {

        super(environment, inputOp1, inputOp2);

        this.input1TypeInfo = getEnvironment().getProperties().input1Type;

        this.input2TypeInfo = getEnvironment().getProperties().input2Type;

        this.buildSide = new HashMap<>();

        // sanity check.
        if (getEnvironment().getProperties().keyIndices1.length != getEnvironment().getProperties().keyIndices1.length)
            throw new IllegalStateException("keyIndices1.length != keyIndices2.length");
        // TODO: check types!
    }

    // ---------------------------------------------------
    // Public Methods.
    // ---------------------------------------------------

    @Override
    public void open() throws Throwable {
        super.open();

        I1 in1 = null;

        // Construct build-side. Consume from <code>inputOp1<code> all
        // tuples and store them in a simple HashMap.
        inputOp1.open();

        in1 = inputOp1.next();

        while (in1 != null) {
            final List<Integer> key1 = new ArrayList<>(getEnvironment().getProperties().keyIndices1.length);

            for (final int[] selectorChain : getEnvironment().getProperties().keyIndices1) {
                key1.add(input1TypeInfo.selectField(selectorChain, in1).hashCode());
            }

            buildSide.put(key1, in1);
            in1 = inputOp1.next();
        }

        inputOp1.close();

        inputOp2.open();
    }

    @Override
    public Tuple2<I1,I2> next() throws Throwable {

        I1 in1 = null;
        I2 in2 = null;

        while (in1 == null) {
            in2 = inputOp2.next();
            if (in2 != null) {
                final List<Integer> key2 = new ArrayList<>(getEnvironment().getProperties().keyIndices2.length);

                for (final int[] selectorChain : getEnvironment().getProperties().keyIndices2) {
                    key2.add(input1TypeInfo.selectField(selectorChain, in2).hashCode());
                }

                in1 = buildSide.get(key2);
            } else {
                return null;
            }
        }

        return new Tuple2<>(in1, in2);
    }

    @Override
    public void close() throws Throwable {
        super.close();
        inputOp2.close();
    }

    /**
     *
     * @param visitor
     */
    @Override
    public void accept(final IVisitor<IPhysicalOperator> visitor) {
        visitor.visit(this);
    }
}