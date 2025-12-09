package taller

import scala.util.Random
import org.scalatest.funsuite.AnyFunSuite

class RiegoParaleloTest extends AnyFunSuite {

  // ---- Generador seguro ----
  def generarEntradas(n: Int, seed: Int) = {
    val r = new Random(seed)
    val f = Riego.fincaAlAzarconRandom(r, n)
    val d = Riego.distanciaAlAzarconRandom(r, n)
    val pi = Riego.generarProgramaciones(f).head   // válido solo N ≤ 10
    (f, d, pi)
  }

  // ============================================================
  //  TESTS ORIGINALES (CON TIEMPOS)
  // ============================================================

  test("Costo de riego secuencial vs paralelo con tiempos") {
    val (f, _, pi) = generarEntradas(10, 100)

    val t0 = System.nanoTime()
    val sec = Riego.costoRiegoFinca(f, pi)
    val t1 = System.nanoTime()

    val t2 = System.nanoTime()
    val par = RiegoParalelo.costoRiegoFincaPar(f, pi)
    val t3 = System.nanoTime()

    println(s"Tiempo secuencial costoRiegoFinca = ${(t1 - t0) / 1e6} ms")
    println(s"Tiempo paralelo   costoRiegoFincaPar = ${(t3 - t2) / 1e6} ms")

    assert(sec == par)
  }

  test("Costo de movilidad secuencial vs paralelo con tiempos") {
    val (f, d, pi) = generarEntradas(10, 101)

    val t0 = System.nanoTime()
    val sec = Riego.costoMovilidad(f, pi, d)
    val t1 = System.nanoTime()

    val t2 = System.nanoTime()
    val par = RiegoParalelo.costoMovilidadPar(f, pi, d)
    val t3 = System.nanoTime()

    println(s"Tiempo secuencial costoMovilidad = ${(t1 - t0) / 1e6} ms")
    println(s"Tiempo paralelo   costoMovilidadPar = ${(t3 - t2) / 1e6} ms")

    assert(sec == par)
  }

  test("Generación de programaciones con tiempos") {
    val f = Riego.fincaAlAzar(8)

    val t0 = System.nanoTime()
    val sec = Riego.generarProgramaciones(f).toSet
    val t1 = System.nanoTime()

    val t2 = System.nanoTime()
    val par = RiegoParalelo.generarProgramacionesRiegoPar(f).toSet
    val t3 = System.nanoTime()

    println(s"Tiempo secuencial generarProgramaciones = ${(t1 - t0) / 1e6} ms")
    println(s"Tiempo paralelo generarProgramacionesRiegoPar = ${(t3 - t2) / 1e6} ms")

    assert(sec == par)
  }

  test("Programación óptima secuencial vs paralelo con tiempos") {
    val (f, d, _) = generarEntradas(8, 200)

    val t0 = System.nanoTime()
    val (_, costoSec) = Riego.programacionOptima(f, d)
    val t1 = System.nanoTime()

    val t2 = System.nanoTime()
    val (_, costoPar) = RiegoParalelo.ProgramacionRiegoOptimoPar(f, d)
    val t3 = System.nanoTime()

    println(s"Tiempo secuencial programacionOptima = ${(t1 - t0) / 1e6} ms")
    println(s"Tiempo paralelo   ProgramacionRiegoOptimoPar = ${(t3 - t2) / 1e6} ms")

    assert(costoSec == costoPar)
  }

  // ============================================================
  //  TESTS NUEVOS GENERADOS (FUNCIONALES SIN TIEMPOS)
  // ============================================================

  test("Paralelo vs Secuencial — costoRiegoFinca (N=12)") {
    val r = new Random(1001)
    val f  = Riego.fincaAlAzarconRandom(r, 12)
    val pi = (0 until 12).toVector
    assert(Riego.costoRiegoFinca(f, pi) == RiegoParalelo.costoRiegoFincaPar(f, pi))
  }

  test("Paralelo vs Secuencial — costoMovilidad (N=20)") {
    val r = new Random(2001)
    val f = Riego.fincaAlAzarconRandom(r, 20)
    val d = Riego.distanciaAlAzarconRandom(r, 20)
    val pi = (0 until 20).toVector
    assert(Riego.costoMovilidad(f, pi, d) == RiegoParalelo.costoMovilidadPar(f, pi, d))
  }

  test("Paralelo vs Secuencial — generarProgramaciones (N=8)") {
    val f = Riego.fincaAlAzar(8)
    assert(Riego.generarProgramaciones(f).toSet ==
      RiegoParalelo.generarProgramacionesRiegoPar(f).toSet)
  }

  test("Paralelo vs Secuencial — programacionOptima (N=8)") {
    val (f, d, _) = generarEntradas(8, 3001)
    val (_, sec)  = Riego.programacionOptima(f, d)
    val (_, par)  = RiegoParalelo.ProgramacionRiegoOptimoPar(f, d)
    assert(sec == par)
  }

  test("Paralelo vs Secuencial — validación mixta (N=10)") {
    val (f, d, pi) = generarEntradas(10, 4001)
    assert(Riego.costoRiegoFinca(f, pi) == RiegoParalelo.costoRiegoFincaPar(f, pi))
    assert(Riego.costoMovilidad(f, pi, d) == RiegoParalelo.costoMovilidadPar(f, pi, d))
  }

}