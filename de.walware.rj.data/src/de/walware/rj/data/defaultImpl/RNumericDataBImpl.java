/*******************************************************************************
 * Copyright (c) 2009-2012 Stephan Wahlbrink (www.walware.de/goto/opensource)
 * and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * v2.1 or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.data.defaultImpl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import de.walware.rj.data.RJIO;


public class RNumericDataBImpl extends AbstractNumericData
		implements RDataResizeExtension, ExternalizableRStore, Externalizable {
	
	
	protected double[] realValues;
	
	
	public RNumericDataBImpl() {
		this.realValues = EMPTY_DOUBLE_ARRAY;
		this.length = 0;
	}
	
	public RNumericDataBImpl(final int length) {
		this.realValues = new double[length];
		this.length = length;
	}
	
	public RNumericDataBImpl(final double[] values, final int[] naIdxs) {
		this.realValues = values;
		this.length = values.length;
		if (naIdxs != null) {
			for (int i = 0; i < naIdxs.length; i++) {
				this.realValues[naIdxs[i]] = NA_numeric_DOUBLE;
			}
		}
	}
	
	public RNumericDataBImpl(final double[] values) {
		this.realValues = values;
		this.length = values.length;
	}
	
	
	public RNumericDataBImpl(final RJIO io) throws IOException {
		readExternal(io);
	}
	
	public void readExternal(final RJIO io) throws IOException {
		this.realValues = io.readDoubleArray();
		this.length = this.realValues.length;
	}
	
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		this.length = in.readInt();
		this.realValues = new double[this.length];
		for (int i = 0; i < this.length; i++) {
			this.realValues[i] = Double.longBitsToDouble(in.readLong());
		}
	}
	
	public void writeExternal(final RJIO io) throws IOException {
		io.writeDoubleArray(this.realValues, this.length);
	}
	
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeInt(this.length);
		for (int i = 0; i < this.length; i++) {
			out.writeLong(Double.doubleToRawLongBits(this.realValues[i]));
		}
	}
	
	
	@Override
	protected final boolean isStructOnly() {
		return false;
	}
	
	
	@Override
	public double getNum(final int idx) {
		return this.realValues[idx];
	}
	
	@Override
	public boolean isNA(final int idx) {
		return (Double.isNaN(this.realValues[idx])
				&& (int) Double.doubleToRawLongBits(this.realValues[idx]) == NA_numeric_INT_MATCH);
	}
	
	public boolean isNaN(final int idx) {
		return (Double.isNaN(this.realValues[idx])
				&& (int) Double.doubleToRawLongBits(this.realValues[idx]) != NA_numeric_INT_MATCH);
	}
	
	public boolean isMissing(final int idx) {
		return (Double.isNaN(this.realValues[idx]));
	}
	
	@Override
	public void setNum(final int idx, final double value) {
		this.realValues[idx] = (Double.isNaN(value)) ? NaN_numeric_DOUBLE : value;
	}
	
	@Override
	public void setNA(final int idx) {
		this.realValues[idx] = NA_numeric_DOUBLE;
	}
	
	private void prepareInsert(final int[] idxs) {
		this.realValues = prepareInsert(this.realValues, this.length, idxs);
		this.length += idxs.length;
	}
	
	public void insertNum(final int idx, final double value) {
		prepareInsert(new int[] { idx });
		this.realValues[idx] = (Double.isNaN(value)) ? NaN_numeric_DOUBLE : value;
	}
	
	public void insertNA(final int idx) {
		prepareInsert(new int[] { idx });
		this.realValues[idx] = NA_numeric_DOUBLE;
	}
	
	public void insertNA(final int[] idxs) {
		if (idxs.length == 0) {
			return;
		}
		prepareInsert(idxs);
		for (int idx = 0; idx < idxs.length; idx++) {
			this.realValues[idx] = NA_numeric_DOUBLE;
		}
	}
	
	public void remove(final int idx) {
		this.realValues = remove(this.realValues, this.length, new int[] { idx });
		this.length--;
	}
	
	public void remove(final int[] idxs) {
		this.realValues = remove(this.realValues, this.length, idxs);
		this.length -= idxs.length;
	}
	
	public Double get(final int idx) {
		if (idx < 0 || idx >= this.length) {
			throw new IndexOutOfBoundsException();
		}
		return (!Double.isNaN(this.realValues[idx])
				|| (int) Double.doubleToRawLongBits(this.realValues[idx]) != NA_numeric_INT_MATCH) ?
			 Double.valueOf(this.realValues[idx]) : null;
	}
	
	@Override
	public Double[] toArray() {
		final Double[] array = new Double[this.length];
		for (int i = 0; i < this.length; i++) {
			if (!Double.isNaN(this.realValues[i])
					|| (int) Double.doubleToRawLongBits(this.realValues[i]) != NA_numeric_INT_MATCH) {
				array[i] = Double.valueOf(this.realValues[i]);
			}
		}
		return array;
	}
	
}
