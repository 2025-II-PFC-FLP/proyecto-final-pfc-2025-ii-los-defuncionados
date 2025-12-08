package taller

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RiegoTest extends AnyFunSuite {


  case class Finca(tiempos: Vector[Int]) {
    val length: Int = tiempos.length
    def tIR(i: Int): Int = tiempos(i)  // tiempo directo para pruebas
  }

  // Distancia como función
  type Distancia = (Int, Int) => Int
  // Programación de riego

  type ProgRiego = Vector[Int]

  // 2.5 Generación de programaciones
  // Genera todas las permutaciones posibles del vector de tablones.
  // Esto me permite obtener todas las rutas válidas de riego.
  def permutaciones(tablas: Vector[Int]): Vector[ProgRiego] =
    if (tablas.isEmpty) Vector(Vector())
    else tablas.flatMap(t =>
      permutaciones(tablas.filter(_ != t)).map(t +: _)
    )

  // Genera todas las programaciones posibles según el tamaño de la finca.
  def generarProgramaciones(f: Finca): Vector[ProgRiego] =
    permutaciones((0 until f.length).toVector)

  // Validación: no se repiten tablones.
  def noRepiteTablones(pi: ProgRiego): Boolean =
    pi.distinct.length == pi.length

  // Validación: la programación tiene todos los tablones.
  def contieneTodos(f: Finca, pi: ProgRiego): Boolean =
    pi.toSet == (0 until f.length).toSet

  // 2.6 Programación optima

  // aquí solo sumo los tiempos de cada tablon
  def costoRiegoFinca(f: Finca, pi: ProgRiego): Int =
    pi.map(f.tIR).sum

  // suma distancia entre tablones consecutivos.
  def costoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int =
    pi.sliding(2).map { case Vector(a, b) => d(a, b) }.sum

  // Costo total = riego + movilidad.
  def costoTotal(f: Finca, pi: ProgRiego, d: Distancia): Int =
    costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d)

  // Calcula la programación optima revisando todas las permutaciones
  def programacionOptima(f: Finca, d: Distancia): (ProgRiego, Int) = {
    val todas = generarProgramaciones(f)
    todas.map(pi => (pi, costoTotal(f, pi, d))).minBy(_._2)
  }

  // Solo retorna la ruta óptima (sin el costo)
  def mejorRuta(f: Finca, d: Distancia): ProgRiego =
    programacionOptima(f, d)._1



  // Prueba 1: 2 tablones
  test("Generar programaciones con 2 tablones") {
    val f = Finca(Vector(3, 5))
    val progs = generarProgramaciones(f)
    assert(progs.contains(Vector(0,1)))
    assert(progs.contains(Vector(1,0)))
    assert(progs.length == 2)        // 2! = 2
  }


  // Prueba 2: 5 tablones (120 permutaciones)
  test("Generar programaciones con 5 tablones") {
    val f = Finca(Vector(1,2,3,4,5))
    val progs = generarProgramaciones(f)
    assert(progs.length == 120)      // 5! = 120
  }

  // Prueba 3: validar que no se repiten
  test("No se repiten tablones en una programacion") {
    val pi = Vector(0,1,2,3)
    assert(noRepiteTablones(pi))
    val piBad = Vector(0,1,1,3)
    assert(!noRepiteTablones(piBad))
  }

  // Prueba 4: validar que contienen todos
  test("Todas las programaciones contienen todos los tablones") {
    val f = Finca(Vector(4,4,4))
    val progs = generarProgramaciones(f)

    assert(progs.forall(pi => contieneTodos(f, pi)))
  }

  // Prueba 5: costo total simple
  test("Costo total se calcula bien en un caso simple") {
    val f = Finca(Vector(2, 3, 1))
    val d: Distancia = (a,b) => Math.abs(a-b)

    val pi = Vector(0,2,1)

    val costo = costoTotal(f, pi, d)

    assert(costo == 9)
  }


  //optimo

  test("Programacion optima en caso pequeño") {
    val f = Finca(Vector(5, 1, 2))
    val d: Distancia = (a,b) => Math.abs(a-b)
    val (piOpt, costo) = programacionOptima(f, d)
    assert(contieneTodos(f, piOpt))
    assert(noRepiteTablones(piOpt))
    val costoManual = piOpt.map(f.tIR).sum +
      piOpt.sliding(2).map{case Vector(a,b) => d(a,b)}.sum
    assert(costo == costoManual)
  }

  test("Comparación secuencial vs manual - finca de 3 tablones") {
    val f = Finca(Vector(3,1,4))
    val d: Distancia = (a,b) => Math.abs(a-b)
    val (piOpt, costo) = programacionOptima(f, d)
    val manual = generarProgramaciones(f)
      .map(pi => (pi, costoTotal(f, pi, d))).minBy(_._2)
    assert(piOpt == manual._1)
    assert(costo == manual._2)
  }

}