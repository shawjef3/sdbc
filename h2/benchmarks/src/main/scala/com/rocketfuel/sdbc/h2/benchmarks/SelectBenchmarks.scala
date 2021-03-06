package com.rocketfuel.sdbc.h2.benchmarks

import com.rocketfuel.sdbc.H2._
import java.util.UUID
import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations._
import scalaz.effect.IO

@State(Scope.Thread)
class SelectBenchmarks {

  implicit val connection =
    Connection.get("jdbc:h2:mem:benchmark;DB_CLOSE_DELAY=0")

  @Param(Array("0", "1", "2", "4", "8", "16", "32", "64", "128", "256", "512"))
  var valueCount: Int = _

  var values: Vector[TestTable] = _

  def createValues(): Vector[TestTable] = {
    val r = new util.Random()

    val randomClasses =
      for {
        i <- 0 until valueCount
      } yield {
        val str1Length = r.nextInt(20)
        val str1 = r.nextString(str1Length)
        val uuid = UUID.randomUUID()
        val str2Length = r.nextInt(20)
        val str2 = r.nextString(str2Length)
        TestTable(0, str1, uuid, str2)
      }

    randomClasses.toVector
  }

  @Setup(Level.Iteration)
  def setup(): Unit = {
    values = createValues()
    TestTable.create.ignore()
    BatchBenchmarks.createSdbcBatch(values).batch()
  }

  @TearDown(Level.Iteration)
  def teardown(): Unit = {
    TestTable.drop.ignore()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def sdbc(): Unit = {
    TestTable.select.vector()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def jdbc(): Unit = {
    val s = connection.prepareStatement(TestTable.select.queryText)

    val rs = s.executeQuery()

    rs.iterator().map(TestTable(_)).toVector
    s.close()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def doobie(): Unit = {
    TestTable.doobieMethods.select.vector.transK[IO].run(connection).unsafePerformIO()
  }

}
