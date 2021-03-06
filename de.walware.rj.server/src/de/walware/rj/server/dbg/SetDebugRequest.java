/*******************************************************************************
 * Copyright (c) 2011-2012 Stephan Wahlbrink (www.walware.de/goto/opensource)
 * and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * v2.1 or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.rj.server.dbg;

import java.io.IOException;

import de.walware.rj.data.RJIO;
import de.walware.rj.data.RJIOExternalizable;


public class SetDebugRequest implements RJIOExternalizable {
	
	public static final int FRAME =                       0x00000001;
	public static final int FUNCTION =                    0x00000002;
	
	private static final int ENABLED =                     0x01000000;
	private static final int TEMP =                        0x00000100;
	
	
	private final long frameId;
	private final String fName;
	
	private final int properties;
	
	
	public SetDebugRequest(final long frameId, final boolean enable, final boolean temp) {
		this.frameId = frameId;
		this.fName = null;
		int properties = FRAME;
		if (enable) {
			properties |= ENABLED;
		}
		if (temp) {
			properties |= TEMP;
		}
		this.properties = properties;
	}
	
	public SetDebugRequest(final int position, final String fName, final boolean enable, final boolean temp) {
		this.frameId = position;
		this.fName = fName;
		int properties = FUNCTION;
		if (enable) {
			properties |= ENABLED;
		}
		if (temp) {
			properties |= TEMP;
		}
		this.properties = properties;
	}
	
	public SetDebugRequest(final RJIO io) throws IOException {
		this.properties = io.readInt();
		switch (this.properties & 0xf) {
		case FRAME:
			this.frameId = io.readLong();
			this.fName = null;
			break;
		case FUNCTION:
			this.frameId = io.readInt();
			this.fName = io.readString();
			break;
		default:
			this.frameId = 0;
			this.fName = null;
			break;
		}
	}
	
	public void writeExternal(final RJIO io) throws IOException {
		io.writeInt(this.properties);
		switch (this.properties & 0xf) {
		case FRAME:
			io.writeLong(this.frameId);
			break;
		case FUNCTION:
			io.writeInt((int) this.frameId);
			io.writeString(this.fName);
			break;
		default:
			break;
		}
	}
	
	
	public int getType() {
		return (this.properties & 0xf);
	}
	
	public long getHandle() {
		return this.frameId;
	}
	
	public int getPosition() {
		return (int) this.frameId;
	}
	
	public String getName() {
		return this.fName;
	}
	
	public int getDebug() {
		return ((this.properties & 0xff000000) >>> 24);
	}
	
	public boolean isTemp() {
		return ((this.properties & TEMP) != 0);
	}
	
}
