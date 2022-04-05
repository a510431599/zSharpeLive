package zx.zxlive.io.test;

import zx.zxlive.common.io.amf3.IDataInput;
import zx.zxlive.common.io.amf3.IDataOutput;
import zx.zxlive.common.io.amf3.IExternalizable;

/**
 * Used for testing AMF3 Vectors
 * 
 * @author Paul
 */
public class Foo implements IExternalizable {

    @Override
    public void readExternal(IDataInput input) {
    }

    @Override
    public void writeExternal(IDataOutput output) {
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Foo[]";
    }
}