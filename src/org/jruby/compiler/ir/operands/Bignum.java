package org.jruby.compiler.ir.operands;

import java.math.BigInteger;
import org.jruby.RubyBignum;
import org.jruby.runtime.ThreadContext;

/**
 * Represents a literal Bignum.
 * 
 * We cache the value so that when the same Bignum Operand is copy-propagated 
 * across multiple instructions, the same RubyBignum object is created.  In a
 * ddition, the same constant across loops should be the same object.
 * 
 * So, in this example, the output should be false, true, true
 * <pre>
 *   n = 0
 *   olda = nil
 *   while (n < 3)
 *     a = 81402749386839761113321
 *     p a.equal?(olda)
 *     olda = a
 *     n += 1
 *   end
 * </pre>
 * 
 */
public class Bignum extends ImmutableLiteral {
    final public BigInteger value;

    public Bignum(BigInteger value) {
        this.value = value;
    }
    
    @Override
    public Object createCacheObject(ThreadContext context) {
        return RubyBignum.newBignum(context.getRuntime(), value);
    }

    @Override
    public String toString() { 
        return value + ":bignum";
    }
}
