package zx.zxlive.io.test;

import zx.zxlive.common.io.amf3.IDataInput;
import zx.zxlive.common.io.amf3.IDataOutput;
import zx.zxlive.common.io.amf3.IExternalizable;

/**
 * Used for testing AMF3 Vectors
 * 
 * @author Paul
 */
public class Foo3 implements IExternalizable {

    private int foo;

    public void setFoo3(int foo) {
        this.foo = foo;
    }

    public int getFoo() {
        return foo;
    }

    @Override
    public void readExternal(IDataInput input) {
        this.foo = input.readInt();
    }

    @Override
    public void writeExternal(IDataOutput output) {
        output.writeInt(foo);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Foo3 [foo=" + foo + "]";
    }

}