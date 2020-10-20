package utils

import chisel3._
import chisel3.util._

class CircularQueuePtr(val entries: Int) extends Bundle {

  val PTR_WIDTH = log2Up(entries)

  val flag = Bool()
  val value = UInt(PTR_WIDTH.W)

}

trait HasCircularQueuePtrHelper {

  implicit class QueuePtrHelper[T <: CircularQueuePtr](ptr: T) {

    final def +(v: UInt): T = {
      val entries = ptr.entries
      val new_ptr = Wire(ptr.cloneType)
      if(isPow2(entries)){
        new_ptr := (Cat(ptr.flag, ptr.value) + v).asTypeOf(new_ptr)
      } else {
        val new_value = ptr.value +& v
        val diff = Cat(0.U(1.W), new_value).asSInt() - Cat(0.U(1.W), (entries-1).U).asSInt()
        val reverse_flag = diff > 0.S
        new_ptr.flag := Mux(reverse_flag, !ptr.flag, ptr.flag)
        new_ptr.value := Mux(reverse_flag,
          diff.asUInt(),
          new_value
        )
      }
      new_ptr
    }

    final def === (that_ptr: T): Bool = ptr.asUInt()===that_ptr.asUInt()
  }


  def isEmpty[T <: CircularQueuePtr](enq_ptr: T, deq_ptr: T): Bool = {
    enq_ptr === deq_ptr
  }

  def isFull[T <: CircularQueuePtr](enq_ptr: T, deq_ptr: T): Bool = {
    (enq_ptr.flag =/= deq_ptr.flag) && (enq_ptr.value === deq_ptr.value)
  }

}
