package taller

import scala.annotation.meta.param
import scala.util.Random
import scala.annotation.tailrec

object Riego {

  // Definimos los tipos
  type Tablon = (Int, Int, Int) // Esto hace referencia al tiempo de supervivencia, tiempo de regado y prioridad
  type Finca = Vector[Tablon] // Este es el vector de tablones
  type Distancia = Vector[Vector[Int]] // Esta es la matriz de distancia entre los tablones
  type ProgRiego = Vector[Int] // Vector para la programacion de riego
  type TiempoInicioRiego = Vector[Int] // Vector con el tiempo de inicio del riego

  // Es el random que utilizamos en algunas funciones
  val random = new Random()

  // 2.1 GENERACION DE ENTRADAS ALEATORIAS

  // Generamos una Finca totalmente aleatoria utilizando el random normal
  def fincaAlAzar(long: Int): Finca = // Aqui implementamos el generar numero de tablones. (el parametro long define el numero de tablones)
    Vector.fill(long)(
      (random.nextInt(long * 2) + 1,
        random.nextInt(long) + 1, // Aqui implementamos el generar duracion de riego (Este valor es el tiempo del regado)
        random.nextInt(4) + 1)
    )

  // Generamos una Finca usando un Random dado (con el fin de las pruebas que se puedan repetir)
  def fincaAlAzarconRandom(r: Random, long: Int): Finca = // Aqui tambien implementamos el generar numero de tablones.
    Vector.fill(long)(
      (r.nextInt(long * 2) + 1,
        r.nextInt(long) + 1,
        r.nextInt(4) + 1)
    )

  // Generamos una matriz de distancia simetrica con el Random normal
  def distanciaAlAzar(long: Int): Distancia = { // Aqui implementamos el generar distancias
    val v = Vector.fill(long, long)(random.nextInt(long * 3) + 1)
    Vector.tabulate(long, long)((i, j) =>
      if (i == j) 0
      else if (i < j) v(i)(j)
      else v(j)(i)
    )
  }

  // Generamos una matriz de distancia con el Random pero ahora dado
  def distanciaAlAzarconRandom(r: Random, long: Int): Distancia = { // Aqui tambien implementamos el generar distancias
    val v = Vector.fill(long, long)(r.nextInt(long * 3) + 1)
    Vector.tabulate(long, long)((i, j) =>
      if (i == j) 0
      else if (i < j) v(i)(j)
      else v(j)(i)
    )
  }

  // 2.2 EXPLORACION DE ENTRADAS

  // Estos son getters para sacar la informacion de componentes concretos de un tablon
  def tsup(f: Finca, i: Int): Int = f(i)._1 // Tiempo de supervivencia

  def treg(f: Finca, i: Int): Int = f(i)._2 // Tiempo de regado

  def prio(f: Finca, i: Int): Int = f(i)._3 // Prioridad

  // Intentamos mostrar la finca de forma "legible"
  def mostrarFinca(f: Finca): String = {
    val header = "Tablon | Tiempo_supervivencia | Tiempo_regado | Prioridad\n"
    val rows = f.zipWithIndex.map { case ((ts, tr, p), i) =>
      f"   $i%2d    |    $ts%3d    |    $tr%2d    |    $p%1d"
    }.mkString("\n")
    s"$header\n$rows"
  }

  // Intentamos mostrar las distancias de forma "legible"
  def mostrarDistancias(d: Distancia): String = { // Aqui implementamos las distancias
    val n = d.length
    val header = "  |   " + (0 until n).map(i => f"$i%3d").mkString
    val separator = "---+" + ("----" * n)
    val rows = d.zipWithIndex.map { case (fila, i) =>
      f" $i%2d | " + fila.map(v => f"$v%3d").mkString
    }.mkString("\n")
    s"$header\n$separator\n$rows"
  }


  // 2.3 CALCULO DEL TIEMPO DE INICIO DEL RIEGO
  /*
  @param // Finca con n tablones (f)
  @param // Programacion de riego (pi)
  @return // Vector para t(i) donde este es el tiempo de inicio del tablon i
*/
  // Implementamos tIR que nos ayuda a calcular el tiempo de inicio del riego con recursion de coola.
  def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
    @tailrec
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

  // VALIDACIONES PARA FINCA, DISTANCIA, PROGRIEGO

  // Validamos que una finca tenga valores razonables
  def validarFinca(f: Finca): Boolean =
    f.forall { case (ts, tr, p) =>
      ts > 0 && tr > 0 && p > 0 && p <= 4
    }

  // Validamos que una matriz de distancia sea simetrica y tenga valores razonables
  def validarDistancias(d: Distancia): Boolean =
    d.indices.forall(i =>
      d(i).length == d.length &&
        d(i)(i) == 0 &&
        d.indices.forall(j => d(i)(j) == d(j)(i) && d(i)(j) >= 0)
    )

  // Validamos que una programacion sea una permutacion valida
  def validarProgRiego(pi: ProgRiego, n: Int): Boolean =
    pi.length == n && pi.toSet == (0 until n).toSet

  /**
   * 2.4.1 costoRiegoTablón (formal)
   *
   * @param i  índice del tablón
   * @param f  finca
   * @param pi programación de riego (vector donde pi(turno) = índice de tablón que riega en ese turno)
   * @return costo entero según la fórmula del enunciado
   */
  def costoRiegoTablon(i: Int, f: Finca, pi: ProgRiego): Int = {
    require(i >= 0 && i < f.length, s"Índice de tablón fuera de rango: $i")
    require(validarProgRiego(pi, f.length), "ProgRiego inválida")
    val t = tIR(f, pi) // vector con los tiempos de inicio t(i)
    val ts_i = tsup(f, i)
    val tr_i = treg(f, i)
    val p_i = prio(f, i)
    val t_i = t(i)
    if (ts_i - tr_i >= t_i) ts_i - (t_i + tr_i)
    else p_i * ((t_i + tr_i) - ts_i)
  }

  def costoRiegoFinca(f: Finca, pi: ProgRiego): Int = {
    require(validarFinca(f), "Finca inválida")
    require(validarProgRiego(pi, f.length), "ProgRiego inválida")
    (0 until f.length).map(i => costoRiegoTablon(i, f, pi)).sum
  }

  /**
   * 2.4.3 costoMovilidad
   * Suma las distancias entre tablones consecutivos según la programación pi.
   * d debe ser matriz simétrica de distancias (validar con validarDistancias).
   */
  def costoMovilidad(f: Finca, pi: ProgRiego, d: Distancia): Int = {
    require(validarProgRiego(pi, f.length), "ProgRiego inválida")
    require(validarDistancias(d), "Matriz de distancias inválida")
    val n = pi.length
    if (n <= 1) 0
    else {
      // pi es vector por turno: pi(turn) = tablon
      (0 until n - 1).map(j => {
        val a = pi(j)
        val b = pi(j + 1)
        d(a)(b)
      }).sum
    }
  }

  def costoRiegoTablonSimple(duracion: Int, caudal: Double, tarifa: Double): Double = {
    require(duracion >= 0, "Duración negativa")
    require(caudal >= 0, "Caudal negativo")
    require(tarifa >= 0, "Tarifa negativa")
    duracion.toDouble * caudal * tarifa
  }


  // 2.5 generacion progrmaciones de riego

  // Esta función genera todas las permutaciones posibles de los tablones
  def permutaciones(tablas: Vector[Int]): Vector[ProgRiego] = {
    if (tablas.isEmpty) Vector(Vector())       // Una sola permutación posible
    else {
      for {
        i <- tablas.indices.toVector
        elem = tablas(i)                       // Tablón fijo
        resto = tablas.patch(i, Nil, 1)        // Vector sin ese tablón
        perm <- permutaciones(resto)           // Permutaciones del resto
      } yield elem +: perm                     // Lo agrego adelante para formar la permutación final
    }
  }

  //genera todas las programaciones posibles segun el número de tablones de la finca, basicamente llama a permutaciones con los indices 0..n-1.
  def generarProgramaciones(f: Finca): Vector[ProgRiego] =
    permutaciones((0 until f.length).toVector)

  // VALIDACIONES
  // Verifica que no existan tablones repetidos en una programación comparando la longitud con la version distinct
  def noRepiteTablones(pi: ProgRiego): Boolean =
    pi.distinct.length == pi.length

  // Verifica que estén todos los tablones necesarios, comparo el conjunto de la programacion con el conjunto esperado
  def contieneTodos(f: Finca, pi: ProgRiego): Boolean =
    pi.toSet == (0 until f.length).toSet

  // 2.6 programacion optima

  // suma costo de riego + costo de movilidad.
  // encapsulo los dos costos para dejarlos más limpios.
  def costoTotal(f: Finca, pi: ProgRiego, d: Distancia): Int =
    costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d)


  // Busca la programación de riego con costo mínimo
  def programacionOptima(f: Finca, d: Distancia): (ProgRiego, Int) = {
    val todas = generarProgramaciones(f)               // Todas las posibles rutas
    todas.map(pi => (pi, costoTotal(f, pi, d)))        // Asocio cada ruta con su costo
      .minBy(_._2)                                     // Me quedo con la de menor costo
  }

  // Solo retorna la programación óptima sin el costo
  def mejorRuta(f: Finca, d: Distancia): ProgRiego =
    programacionOptima(f, d)._1


  /*EJEMPLO DE MAIN PARA VER COMO ESTA FUNCIONANDO EL CODIGO

  def main(args: Array[String]): Unit = {
    println("=" * 60)
    println("PROYECTO DE RIEGO PRIMERA PARTE")
    println("=" * 60)

    // Ejemplo con 3 tablones (pequeño)
    println("\n EJEMPLO CON 3 TABLONES")
    val f1 = fincaAlAzarconRandom(new Random(42), 3)
    println(mostrarFinca(f1))
    // Mostramos la matriz de distancia
    println("\n MATRIZ DE DISTANCIAS")
    val d1 = distanciaAlAzarconRandom(new Random(42), 3)
    println(mostrarDistancias(d1))
    // Mostramos el tiempo de inicio de riego
    println("\n TIEMPO DE INICIO DE RIEGO")
    val pi1 = Vector(0, 1, 2)
    val tir1 = tIR(f1, pi1)
    println(s"Programacion: ${pi1.mkString(", ")}")
    println(s"Tiempos inicio: ${tir1.mkString(", ")}")

    println("\n" + "=" * 60)
    println("=" * 60)
  }

  */


}

