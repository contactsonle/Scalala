/*
 * Distributed as part of Scalala, a linear algebra library.
 *
 * Copyright (C) 2008- Daniel Ramage
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110 USA
 */
package scalala;
package tensor;

import scalar.Scalar;

import domain.Domain1;
import operators.{BinaryOp,OpAdd,OpMul};

import mutable.TensorBuilder;

/**
 * Implementation trait for a one-axis tensor supports methods like norm
 * and inner products (dot) with other one-axis tensors.
 *
 * @author dramage
 */
trait Tensor1Like
[@specialized(Int,Long) K, @specialized(Int,Long,Float,Double) V,
 +D<:Domain1[K], +This<:Tensor1[K,V]]
extends TensorLike[K,V,D,This] { self =>

  /** Returns the number of elements in the domain of this vector. */
  def size = domain.size;

  /** Returns the k-norm of this tensor. */
  def norm(n : Double) : Double = {
    if (n == 1) {
      var sum = 0.0;
      foreachNonZeroValue(v => sum += scalar.norm(v));
      return sum;
    } else if (n == 2) {
      var sum = 0.0;
      foreachNonZeroValue(v => { val nn = scalar.norm(v); sum += nn * nn });
      return math.sqrt(sum);
    } else if (n == Double.PositiveInfinity) {
      var max = Double.NegativeInfinity;
      foreachNonZeroValue(v => { val nn = scalar.norm(v); if (nn > max) max = nn; });
      return max;
    } else {
      var sum = 0.0;
      foreachNonZeroValue(v => { val nn = scalar.norm(v); sum += math.pow(nn,n); });
      return math.pow(sum, 1.0 / n);
    }
  }

  /** Returns the inner product of this tensor with another. */
  def dot[C,R](that : Tensor1[K,C])(implicit mul : BinaryOp[V,C,OpMul,R], add : BinaryOp[R,R,OpAdd,R], scalar : Scalar[R]) : R = {
    checkDomain(that.domain);
    var sum = scalar.zero;
    foreachNonZeroPair((k,v) => sum = add(sum, mul(v, that(k))));
    sum;
  }

  override protected def canEqual(other : Any) : Boolean = other match {
    case that : Tensor1[_,_] => true;
    case _ => false;
  }
}

/**
 * One-axis tensor supports methods like norm
 * and inner products (dot) with other one-axis tensors.
 *
 * @author dramage
 */
trait Tensor1[@specialized(Int,Long) K, @specialized(Int,Long,Float,Double) V]
extends Tensor[K,V] with Tensor1Like[K,V,Domain1[K],Tensor1[K,V]];

object Tensor1 {
  def apply[K,V:Scalar](keys : (K,V)*) : Tensor1Col[K,V] = {
    val m = keys.toMap;
    val s = implicitly[Scalar[V]];
    new Tensor1Col[K,V] {
      override val scalar = s;
      override val domain = scalala.tensor.domain.SetDomain(m.keySet);
      override def apply(key : K) = m(key);
    }
  }
}

