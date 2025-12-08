package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import scala.util.Random

@RunWith(classOf[JUnitRunner])
class RiegoTest extends AnyFunSuite {

  // Definimos los tipos y las funciones auxiliares para los test

  type Tablon = (Int, Int, Int)
  type Finca = Vector[Tablon]
  type Distancia = Vector[Vector[Int]]
  type ProgRiego = Vector[Int]
  type TiempoInicioRiego = Vector[Int]

  def treg(f: Finca, i: Int): Int = f(i)._2

  def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
    def rec(idx: Int, tiempoActual: Int, resultado: TiempoInicioRiego): TiempoInicioRiego =
      if (idx >= pi.length) resultado
      else {
        val tablonActual = pi(idx)
        val nuevoResultado = resultado.updated(tablonActual, tiempoActual)
        val nuevoTiempo = tiempoActual + treg(f, tablonActual)
        rec(idx + 1, nuevoTiempo, nuevoResultado)
      }

    rec(0, 0, Vector.fill(f.length)(0))
  }

  def validarFinca(f: Finca): Boolean =
    f.forall { case (ts, tr, p) => ts > 0 && tr > 0 && p > 0 && p <= 4 }

  def validarDistancias(d: Distancia): Boolean =
    d.indices.forall(i =>
      d(i).length == d.length &&
        d(i)(i) == 0 &&
        d.indices.forall(j => d(i)(j) == d(j)(i) && d(i)(j) >= 0)
    )

  def fincaAlAzarConRandom(r: Random, long: Int): Finca =
    Vector.fill(long)((r.nextInt(long * 2) + 1, r.nextInt(long) + 1, r.nextInt(4) + 1))

  def distanciaAlAzarConRandom(r: Random, long: Int): Distancia = {
    val v = Vector.fill(long, long)(r.nextInt(long * 3) + 1)
    Vector.tabulate(long, long)((i, j) =>
      if (i == j) 0
      else if (i < j) v(i)(j)
      else v(j)(i)
    )
  }

  // Generacion de entradas aleatorias (5 pruebas)

  test("Generar finca con 3 tablones") {
    val f = fincaAlAzarConRandom(new Random(10), 3)
    assert(f.length == 3, "La finca debe tener exactamente 3 tablones")
    assert(validarFinca(f), "Todos los valores deben ser positivos y válidos")
  }

  test("Generar finca con 10 tablones") {
    val f = fincaAlAzarConRandom(new Random(50), 10)
    assert(f.length == 10, "La finca debe tener 10 tablones")
    assert(validarFinca(f), "Todos los valores deben ser positivos y válidos")
    assert(f.forall(_._1 <= 20), "Tiempo supervivencia debe estar en rango [1, long*2]")
    assert(f.forall(_._2 <= 10), "Tiempo regado debe estar en rango [1, long]")
  }

  test("Generar finca con 50 tablones") {
    val f = fincaAlAzarConRandom(new Random(99), 50)
    assert(f.length == 50, "La finca debe tener 50 tablones")
    assert(validarFinca(f), "Todos los valores deben ser positivos y válidos")
  }

  test("Validar que no existan valores negativos en finca generada") {
    val f = fincaAlAzarConRandom(new Random(123), 20)
    assert(f.forall { case (ts, tr, p) => ts > 0 && tr > 0 && p > 0 },
      "No debe haber valores negativos o cero")
    assert(f.forall(_._3 <= 4), "La prioridad debe estar entre 1 y 4")
  }

  test("Validar consistencia de valores generados en matriz de distancias") {
    val d = distanciaAlAzarConRandom(new Random(777), 5)
    assert(d.length == 5, "Matriz debe ser cuadrada de 5x5")
    assert(validarDistancias(d), "Matriz debe ser simétrica con diagonal cero")
    assert(d.forall(_.forall(_ >= 0)), "No debe haber distancias negativas")
  }

  // Test de exploracion de entradas (5 pruebas)

  test("Mostrar finca pequeña") {
    val f: Finca = Vector((5,1,2), (6,1,3), (4,1,1))
    assert(f.length == 3)
    assert(f(0) == (5,1,2))
    assert(f(1) == (6,1,3))
    assert(f(2) == (4,1,1))
  }

  test("Mostrar finca grande") {
    val f = fincaAlAzarConRandom(new Random(888), 100)
    assert(f.length == 100)
    assert(f.indices.forall(i => f(i)._1 > 0 && f(i)._2 > 0 && f(i)._3 >= 1 && f(i)._3 <= 4))
  }

  test("Datos extremos: duración/costo mínimo") {
    val f: Finca = Vector((10,3,1), (2,1,3), (15,8,4))
    val minSupervivencia = f.map(_._1).min
    val minRegado = f.map(_._2).min
    assert(minSupervivencia == 2)
    assert(minRegado == 1)
  }

  test("Validación de reproducibilidad (misma semilla debe ser misma finca)") {
    val r1 = new Random(42)
    val r2 = new Random(42)
    val f1 = fincaAlAzarConRandom(r1, 5)
    val f2 = fincaAlAzarConRandom(r2, 5)
    assert(f1 == f2, "Misma semilla debe generar la misma finca")
  }

  test("Consistencia entre exploraciones consecutivas") {
    val f: Finca = Vector((10,3,4), (5,3,3), (2,2,1))
    val d: Distancia = Vector(
      Vector(0,3,5),
      Vector(3,0,4),
      Vector(5,4,0)
    )
    assert(validarFinca(f), "Finca debe ser válida")
    assert(validarDistancias(d), "Distancias deben ser válidas")
    assert(f(0)._1 == 10)
    assert(d(0)(1) == d(1)(0), "Simetría debe mantenerse")
  }

  // Test del tiempo de inicio de riego (5 pruebas)

  test("Duración pequeña, inicio temprano correcto") {
    val f: Finca = Vector((5,1,2), (6,1,3), (4,1,1))
    val pi = Vector(0, 1, 2)
    val esperado = Vector(0, 1, 2)
    val resultado = tIR(f, pi)
    assert(resultado == esperado,
      s"Esperado $esperado pero obtuvo $resultado")
  }

  test("Duración grande, impacto acumulado correcto") {
    val f: Finca = Vector((20,10,4), (25,15,3), (18,8,2))
    val pi = Vector(0, 1, 2)
    val esperado = Vector(0, 10, 25)
    val resultado = tIR(f, pi)
    assert(resultado == esperado,
      s"Esperado $esperado pero obtuvo $resultado")
  }

  test("Conjunto ordenado de varios tablones") {
    val f: Finca = Vector(
      (10, 3, 4),
      (5, 3, 3),
      (2, 2, 1),
      (8, 1, 1),
      (6, 4, 2)
    )
    val pi = Vector(0, 1, 4, 2, 3)
    val esperado = Vector(0, 3, 10, 12, 6)
    val resultado = tIR(f, pi)
    assert(resultado == esperado,
      s"Esperado $esperado pero obtuvo $resultado")
  }

  test("Verificación con ejemplo del taller") {
    val f: Finca = Vector(
      (10, 3, 4),
      (5, 3, 3),
      (2, 2, 1),
      (8, 1, 1),
      (6, 4, 2)
    )
    val pi1 = Vector(0, 1, 4, 2, 3)
    val tir1 = tIR(f, pi1)
    val esperado = Vector(0, 3, 10, 12, 6)
    assert(tir1 == esperado,
      s"El tiempo de inicio debe coincidir con los tiempos acumulados")
  }

  test("Verificación con orden diferente") {
    val f: Finca = Vector(
      (10, 3, 4),
      (5, 3, 3),
      (2, 2, 1),
      (8, 1, 1),
      (6, 4, 2)
    )
    val pi2 = Vector(2, 1, 4, 3, 0)
    val tir2 = tIR(f, pi2)
    val esperado = Vector(10, 2, 0, 9, 5)
    assert(tir2 == esperado,
      s"Esperado $esperado pero obtuvo $tir2")
  }

  // Test de validacion

  test("Prueba adicional - diferentes semillas generan fincas distintas") {
    val f1 = fincaAlAzarConRandom(new Random(42), 5)
    val f2 = fincaAlAzarConRandom(new Random(99), 5)
    assert(f1 != f2, "Diferentes semillas deben generar fincas distintas")
  }

  test("Prueba adicional - validación de matriz diagonal vacía") {
    val d = distanciaAlAzarConRandom(new Random(555), 10)
    assert(d.indices.forall(i => d(i)(i) == 0),
      "La diagonal debe ser cero")
  }


  /* TESTS DE MARIANA (estos test utilizan su propia estructura de datos dentro de cada test asi no generan conflicto con los anteriorres test)
  Cambie Finca por FincaM, Distancia por DistanciaM ETC esto con el fin de no confundirnos*/

  test("Generar programaciones con 2 tablones") {
    case class FincaM(tiempos: Vector[Int]) {
      val length: Int = tiempos.length
      def tIR(i: Int): Int = tiempos(i)
    }
    type DistanciaM = (Int, Int) => Int
    type ProgRiegoM = Vector[Int]

    def permutaciones(tablas: Vector[Int]): Vector[ProgRiegoM] =
      if (tablas.isEmpty) Vector(Vector())
      else tablas.flatMap(t =>
        permutaciones(tablas.filter(_ != t)).map(t +: _)
      )

    def generarProgramaciones(f: FincaM): Vector[ProgRiegoM] =
      permutaciones((0 until f.length).toVector)

    val f = FincaM(Vector(3, 5))
    val progs = generarProgramaciones(f)
    assert(progs.contains(Vector(0,1)))
    assert(progs.contains(Vector(1,0)))
    assert(progs.length == 2)
  }

  test("Generar programaciones con 5 tablones") {
    case class FincaM(tiempos: Vector[Int]) {
      val length: Int = tiempos.length
      def tIR(i: Int): Int = tiempos(i)
    }
    type ProgRiegoM = Vector[Int]

    def permutaciones(tablas: Vector[Int]): Vector[ProgRiegoM] =
      if (tablas.isEmpty) Vector(Vector())
      else tablas.flatMap(t =>
        permutaciones(tablas.filter(_ != t)).map(t +: _)
      )

    def generarProgramaciones(f: FincaM): Vector[ProgRiegoM] =
      permutaciones((0 until f.length).toVector)

    val f = FincaM(Vector(1,2,3,4,5))
    val progs = generarProgramaciones(f)
    assert(progs.length == 120)
  }

  test("No se repiten tablones en una programacion") {
    type ProgRiegoM = Vector[Int]

    def noRepiteTablones(pi: ProgRiegoM): Boolean =
      pi.distinct.length == pi.length

    val pi = Vector(0,1,2,3)
    assert(noRepiteTablones(pi))
    val piBad = Vector(0,1,1,3)
    assert(!noRepiteTablones(piBad))
  }

  test("Todas las programaciones contienen todos los tablones") {
    case class FincaM(tiempos: Vector[Int]) {
      val length: Int = tiempos.length
      def tIR(i: Int): Int = tiempos(i)
    }
    type ProgRiegoM = Vector[Int]

    def permutaciones(tablas: Vector[Int]): Vector[ProgRiegoM] =
      if (tablas.isEmpty) Vector(Vector())
      else tablas.flatMap(t =>
        permutaciones(tablas.filter(_ != t)).map(t +: _)
      )

    def generarProgramaciones(f: FincaM): Vector[ProgRiegoM] =
      permutaciones((0 until f.length).toVector)

    def contieneTodos(f: FincaM, pi: ProgRiegoM): Boolean =
      pi.toSet == (0 until f.length).toSet

    val f = FincaM(Vector(4,4,4))
    val progs = generarProgramaciones(f)
    assert(progs.forall(pi => contieneTodos(f, pi)))
  }

  test("Costo total, este se calcula bien en un caso simple") {
    case class FincaM(tiempos: Vector[Int]) {
      val length: Int = tiempos.length
      def tIR(i: Int): Int = tiempos(i)
    }
    type DistanciaM = (Int, Int) => Int
    type ProgRiegoM = Vector[Int]

    def costoRiegoFinca(f: FincaM, pi: ProgRiegoM): Int =
      pi.map(f.tIR).sum

    def costoMovilidad(f: FincaM, pi: ProgRiegoM, d: DistanciaM): Int =
      pi.sliding(2).map { case Vector(a, b) => d(a, b) }.sum

    def costoTotal(f: FincaM, pi: ProgRiegoM, d: DistanciaM): Int =
      costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d)

    val f = FincaM(Vector(2, 3, 1))
    val d: DistanciaM = (a,b) => Math.abs(a-b)
    val pi = Vector(0,2,1)
    val costo = costoTotal(f, pi, d)
    assert(costo == 9)
  }

  test("Programacion optima en caso pequeño") {
    case class FincaM(tiempos: Vector[Int]) {
      val length: Int = tiempos.length
      def tIR(i: Int): Int = tiempos(i)
    }
    type DistanciaM = (Int, Int) => Int
    type ProgRiegoM = Vector[Int]

    def permutaciones(tablas: Vector[Int]): Vector[ProgRiegoM] =
      if (tablas.isEmpty) Vector(Vector())
      else tablas.flatMap(t =>
        permutaciones(tablas.filter(_ != t)).map(t +: _)
      )

    def generarProgramaciones(f: FincaM): Vector[ProgRiegoM] =
      permutaciones((0 until f.length).toVector)

    def noRepiteTablones(pi: ProgRiegoM): Boolean =
      pi.distinct.length == pi.length

    def contieneTodos(f: FincaM, pi: ProgRiegoM): Boolean =
      pi.toSet == (0 until f.length).toSet

    def costoRiegoFinca(f: FincaM, pi: ProgRiegoM): Int =
      pi.map(f.tIR).sum

    def costoMovilidad(f: FincaM, pi: ProgRiegoM, d: DistanciaM): Int =
      pi.sliding(2).map { case Vector(a, b) => d(a, b) }.sum

    def costoTotal(f: FincaM, pi: ProgRiegoM, d: DistanciaM): Int =
      costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d)

    def programacionOptima(f: FincaM, d: DistanciaM): (ProgRiegoM, Int) = {
      val todas = generarProgramaciones(f)
      todas.map(pi => (pi, costoTotal(f, pi, d))).minBy(_._2)
    }

    val f = FincaM(Vector(5, 1, 2))
    val d: DistanciaM = (a,b) => Math.abs(a-b)
    val (piOpt, costo) = programacionOptima(f, d)
    assert(contieneTodos(f, piOpt))
    assert(noRepiteTablones(piOpt))
    val costoManual = piOpt.map(f.tIR).sum +
      piOpt.sliding(2).map{case Vector(a,b) => d(a,b)}.sum
    assert(costo == costoManual)
  }

  test("Comparación secuencial vs manual para finca de 3 tablones") {
    case class FincaM(tiempos: Vector[Int]) {
      val length: Int = tiempos.length
      def tIR(i: Int): Int = tiempos(i)
    }
    type DistanciaM = (Int, Int) => Int
    type ProgRiegoM = Vector[Int]

    def permutaciones(tablas: Vector[Int]): Vector[ProgRiegoM] =
      if (tablas.isEmpty) Vector(Vector())
      else tablas.flatMap(t =>
        permutaciones(tablas.filter(_ != t)).map(t +: _)
      )

    def generarProgramaciones(f: FincaM): Vector[ProgRiegoM] =
      permutaciones((0 until f.length).toVector)

    def costoRiegoFinca(f: FincaM, pi: ProgRiegoM): Int =
      pi.map(f.tIR).sum

    def costoMovilidad(f: FincaM, pi: ProgRiegoM, d: DistanciaM): Int =
      pi.sliding(2).map { case Vector(a, b) => d(a, b) }.sum

    def costoTotal(f: FincaM, pi: ProgRiegoM, d: DistanciaM): Int =
      costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d)

    def programacionOptima(f: FincaM, d: DistanciaM): (ProgRiegoM, Int) = {
      val todas = generarProgramaciones(f)
      todas.map(pi => (pi, costoTotal(f, pi, d))).minBy(_._2)
    }

    val f = FincaM(Vector(3,1,4))
    val d: DistanciaM = (a,b) => Math.abs(a-b)
    val (piOpt, costo) = programacionOptima(f, d)
    val manual = generarProgramaciones(f)
      .map(pi => (pi, costoTotal(f, pi, d))).minBy(_._2)
    assert(piOpt == manual._1)
    assert(costo == manual._2)
  }

}