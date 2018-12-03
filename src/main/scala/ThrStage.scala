// Author:  Davide Conficconi
// Date: 10/09/2018
// Revision: 0
package bismo

import Chisel._
import fpgatidbits.ocm._
import fpgatidbits.streams._

class ThrStageParams(
<<<<<<< HEAD
                      // building block params
                      val thuParams : ThresholdingUnitParams,
                      // threshold memory depth (how many entries, address space)
                      val thresholdMemDepth : Int,
                      val inputMemDepth : Int,
                      val resMemDepth : Int,
                      val activationMemoryLatency : Int = 1,
                      // levels of registers before (on address input) and after (on data output)
                      // of each tile memory BRAM
                      val bramInRegs: Int = 1,
                      val bramOutRegs: Int = 1
  ) extends PrintableParam {

  //how many threshold
  val maxThresholdNumber : Int = scala.math.pow(2,thuParams.maxOutputBitPrecision).toInt - 1
  // threshold memory width (how many output bits)

  val thresholdMemWidth : Int = thuParams.inputBitPrecision * maxThresholdNumber

  val thresholdLatency : Int = maxThresholdNumber - thuParams.unrollingFactorOutputPrecision
=======
  // building block params
  val thuParams: ThresholdingUnitParams,
  // threshold memory depth (how many entries, address space)
  val thresholdMemDepth: Int,
  val inputMemAddr: Int,
  val resMemAddr: Int) extends PrintableParam {

  //how many threshold
  val thresholdNumber: Int = scala.math.pow(2, thuParams.maxOutputBitPrecision).toInt - 1
  // threshold memory width (how many output bits)

  val thresholdMemWidth: Int = thuParams.inputBitPrecision * thresholdNumber

  val thresholdLatency: Int = thresholdNumber - thuParams.unrollingFactorOutputPrecision
>>>>>>> feature/p2s-stdalone

  //M of DPA
  def getUnrollRows(): Int = {
    return thuParams.unrollingFactorRows
  }
  def getRows(): Int = {
    return thuParams.matrixRows
  }
  //K of DPA
  def getInBits(): Int = {
    return thuParams.inputBitPrecision
  }
  //ASSUMPTION: No rolling in the columns
  def getCols(): Int = {
    return thuParams.matrixColumns
  }
  //ASSUMPTION: No rolling in the columns
  def getUnrollCols(): Int = {
    return thuParams.unrollingFactorColumns
  }

  def getResBitWidth(): Int = {
    return thuParams.maxOutputBitPrecision
  }
  def getThUnroll() : Int = {
    return  thuParams.unrollingFactorOutputPrecision
  }
  def headersAsList(): List[String] = {
    return thuParams.headersAsList() ++ List("thresholdMemDepth", "thresholdMemWidth")
  }

  def contentAsList(): List[String] = {
    return thuParams.contentAsList() ++ List(thresholdMemDepth, thresholdMemWidth).map(_.toString)
  }
}

// interface to hardware config available to software
class ThrStageCfgIO() extends Bundle {
  val config_thu_th_unroll = UInt(OUTPUT, width = 32)
  //TODO NOT REALLY USED NOW
  override def cloneType: this.type =
    new ThrStageCfgIO().asInstanceOf[this.type]
}

// interface towards controller for the execute stage
class ThrStageCtrlIO(myP: ThrStageParams) extends PrintableBundle {

  //TODO NOT REALLY USED NOW
  val actOffset = UInt(width = 32) // start offset for activation memory
  val thrOffset = UInt(width = 32) // start offset for threshold memory
  val runTimeThrNumber = UInt(width = 32) // programmable parameter for how many thresholds use
  // write to result memory at the end of current execution
  val writeEn = Bool()
  // result memory address to use for writing
  val writeAddr = UInt(width = log2Up(myP.thresholdMemDepth))

  override def cloneType: this.type =
    new ThrStageCtrlIO(myP).asInstanceOf[this.type]

  val printfStr = "(offs lhs/rhs = %d/%d, ntiles = %d, << %d, w? %d/%d)\n"
  val printfElems = { () ⇒
    Seq( //lhsOffset, rhsOffset, numTiles, shiftAmount, writeEn, writeAddr
    )
  }
}

// interface towards tile memories (LHS(Thresholds)/RHS(Activation) BRAMs)
class ThrStageTileMemIO(myP: ThrStageParams) extends Bundle {
  val thr_req = Vec.fill(myP.getUnrollRows()) { Vec.fill(myP.getThUnroll()){
    new OCMRequest(myP.getInBits(), log2Up(myP.thresholdMemDepth)).asOutput
  }}
  val thr_rsp = Vec.fill(myP.getUnrollRows()) { Vec.fill(myP.getThUnroll()){
    new OCMResponse(myP.getInBits() ).asInput
  }}
  val act_req = Vec.fill(myP.getUnrollRows()) { Vec.fill(myP.getUnrollCols()){
    new OCMRequest(myP.getInBits(), log2Up(myP.inputMemDepth)).asOutput
  }}
  val act_rsp = Vec.fill(myP.getUnrollRows()) { Vec.fill(myP.getUnrollCols()){
    new OCMResponse(myP.getInBits()).asInput
  }}

  override def cloneType: this.type =
    new ThrStageTileMemIO(myP).asInstanceOf[this.type]
}

//TODO Assumed able to write one row per request and not in bit serial way
// interface towards result stage
class ThrStageResMemIO(myP: ThrStageParams) extends Bundle {
  val req = Vec.fill(myP.getUnrollRows()) { Vec.fill(myP.getUnrollCols()){
    new OCMRequest(
<<<<<<< HEAD
      myP.getResBitWidth(), log2Up(myP.resMemDepth)
    ).asOutput
  }}
=======
      myP.getResBitWidth() * myP.getCols(), log2Up(myP.resMemAddr)).asOutput
  }
>>>>>>> feature/p2s-stdalone

  override def cloneType: this.type =
    new ThrStageResMemIO(myP).asInstanceOf[this.type]
}

class ThrStage(val myP: ThrStageParams) extends Module {
  val io = new Bundle {
    val start = Bool(INPUT) // hold high while running
    val done = Bool(OUTPUT) // high when done until start=0
    val cfg = new ThrStageCfgIO()
    val ctrl = new ThrStageCtrlIO(myP).asInput
    val res = new ThrStageResMemIO(myP)
    val inMemory = new ThrStageTileMemIO(myP)
  }

<<<<<<< HEAD
=======
  //TODO: ASSUMPTION: fetch from the bram the whole matrix and then start
>>>>>>> feature/p2s-stdalone

  val thu = Module(new ThresholdingUnit(myP.thuParams)).io
  //ASSUMING THIS PARAM
  val seqgen = Module(new SequenceGenerator(log2Up(myP.thresholdMemDepth) + 1)).io

  val thTile = if(myP.getThUnroll() == 1) {io.ctrl.runTimeThrNumber} else { UInt(1, width = myP.maxThresholdNumber)}

  thu.thInterf.thresholdCount := io.ctrl.runTimeThrNumber

  seqgen.init := UInt(0)
<<<<<<< HEAD
  seqgen.count := thTile
  seqgen.step := UInt(1, width = myP.maxThresholdNumber)
=======
  seqgen.count := UInt(myP.getUnrollRows()) //io.csr.numTiles
  seqgen.step := UInt(1) //UInt(myP.tileMemAddrUnit)
>>>>>>> feature/p2s-stdalone
  seqgen.start := io.start
  seqgen.seq.ready := Bool(true)

  //never write into that memory
<<<<<<< HEAD
  for(i <- 0 until myP.getUnrollRows())
    for(j <- 0 until myP.getUnrollCols()){
      io.inMemory.act_req(i)(j).writeEn := Bool(false)
      io.inMemory.act_req(i)(j).writeData := UInt(0)
      //TODO manage exectuion flow in the rolling of the matrix dimensions
      io.inMemory.act_req(i)(j).addr := UInt(0) + io.ctrl.actOffset
  }

  val threshold_address = UInt()
  val threshold_address_reg = Reg(init = UInt(0))
  when(seqgen.seq.valid){
    threshold_address := seqgen.seq.bits + io.ctrl.thrOffset
    threshold_address_reg := seqgen.seq.bits + io.ctrl.thrOffset
  }.otherwise{
    threshold_address := threshold_address_reg + io.ctrl.thrOffset
  }
  for(i <- 0 until myP.getUnrollRows())
    for(j <- 0 until myP.getThUnroll()){
      io.inMemory.thr_req(i)(j).writeEn := Bool(false)
      io.inMemory.thr_req(i)(j).writeData := UInt(0)
      io.inMemory.thr_req(i)(j).addr := threshold_address
    }

  val start_r = Reg(init = false.B, next = io.start & seqgen.seq.valid)
  //TODO this is for a single run per start
  val start_pulse = io.start & seqgen.seq.valid & !start_r | ( start_r & !seqgen.seq.valid  )
  //TODO this is for a continuous run per start
  //val start_pulse = io.start & !start_r

  //ASSUMING that my data are available as long as I need
  for(i <- 0 until myP.getUnrollRows()) {
    for (j <- 0 until myP.getUnrollCols()) {
      thu.inputMatrix.bits.i(i)(j) := io.inMemory.act_rsp(i)(j).readData//(myP.getInBits() * (1 + j) - 1, myP.getInBits() * j)
      
      /************* DEBUG PRINT ************/
      when(start_pulse){
        // Debugger.log("[HW: Thr Stage] Matrix elem "+ UInt(i) + " " +
        //  UInt(j) +" Read elem from Unit: " + 
        //  io.inMemory.act_rsp(i)(j).readData + "\n", 1)
        //printf("[HW: Thr Stage] Matrix elem %d, %d Read elem from Unit: %d\n",UInt(i), , io.inMemory.act_rsp(i)(j).readData)//(myP.getInBits() * (1 + j) - 1, myP.getInBits() * j))
      }
      /************** END ************/
    }
    for(j <- 0 until myP.getThUnroll())
    thu.thInterf.thresholdData(i)(j) := io.inMemory.thr_rsp(i)(j).readData//( myP.getInBits()*(1+j) - 1, myP.getInBits()*j)
  }


  thu.inputMatrix.valid := ShiftRegister(start_pulse, myP.activationMemoryLatency-1)

=======
  for (i ← 0 until myP.getUnrollRows()) {
    io.inMemory.act_req(i).writeEn := Bool(false)
    io.inMemory.act_req(i).writeData := UInt(0)
    io.inMemory.act_req(i).addr := UInt(i)
    io.inMemory.thr_req(i).writeEn := Bool(false)
    io.inMemory.thr_req(i).writeData := UInt(0)
    io.inMemory.thr_req(i).addr := UInt(i)
  }

  //ASSUMING that my data are available as long as I need
  //Not convinced at all
  for (i ← 0 until myP.getUnrollRows()) {
    for (j ← 0 until myP.getUnrollCols()) {
      thu.inputMatrix.bits.i(i)(j) := io.inMemory.act_rsp(i).readData(myP.getInBits() * (1 + j) - 1, myP.getInBits() * j)
    }
    for (j ← 0 until myP.thresholdNumber)
      thu.thInterf.thresholdData(i)(j) := io.inMemory.thr_rsp(i).readData(myP.getInBits() * (1 + j) - 1, myP.getInBits() * j)
  }

>>>>>>> feature/p2s-stdalone
  //time to write is the latency of the unit + time to write all the outputs
  //TODO this is not at run time.... so if less thresholds with a rolled version I waste some cycles in this way
  val time_to_write = myP.thuParams.getLatency()
  //TODO: this is time to respond for the Data BRAM
  val end = ShiftRegister(thu.outputMatrix.valid, time_to_write)
<<<<<<< HEAD
  val end_r = Reg(init = false.B, next = end)
  val end_pulse = end & !end_r
  io.done := (end_pulse | end_r)
  /*************DEBUG PRINT*************/
//  when(end_pulse | end_r){
//    Debugger.log("[HW: Thr Stage] Thresholding stage Done\n", 0)
//    // printf("[HW: Thr Stage] Thresholding stage Done\n")
//  }
  /*************   END   *************/

  val i = Reg(init = UInt(0, width = 32))
  /*
    ************DEBUG PRINT************
  when(end){
    printf("[HW] It takes %d cycles to restart!!!\n",i);
    i := i + UInt(1)
  }*/
  thu.outputMatrix.ready := end & !io.start
  for(i <- 0 until myP.getUnrollRows())
    for(j <- 0 until myP.getUnrollCols()){
      io.res.req(i)(j).writeEn := thu.outputMatrix.valid
      io.res.req(i)(j).addr := UInt(0)
      io.res.req(i)(j).writeData := thu.outputMatrix.bits.o(i)(j)//UInt(0, width = myP.getResBitWidth() * myP.getCols()) | (thu.outputMatrix.bits.o(i)(j) << j)
=======
  io.done := end

  for (i ← 0 until myP.getRows())
    for (j ← 0 until myP.getCols()) {
      io.res.req(i).writeEn := thu.outputMatrix.valid
      io.res.req(i).addr := UInt(i)
      io.res.req(i).writeData := UInt(0, width = myP.getResBitWidth() * myP.getCols()) | (thu.outputMatrix.bits.o(i)(j) << j)
>>>>>>> feature/p2s-stdalone
    }

}
