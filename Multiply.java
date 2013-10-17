
import java.util.Arrays;
import java.util.Collections;

/**
 * Class for vector and square matrix multiplication.
 *
 * @author Zofia Sobocinska
 */
public class Multiply {

  static final int THREADS_COUNT = 4;

  /**
   * Multiplication method. It creates THREADS_COUNT threads to
   * multiply faster.
   *
   * @param a Matrix being multiplication's factor
   * @param x Vector being multiplication's factor
   * @return y Vector being multiplication's output
   */
  public static Vector multiply(Matrix a,
    Vector x) {

    MultiplyThread[] threads =
      new MultiplyThread[THREADS_COUNT];
    int rowsCount = a.n();
    int rowsPerThread = rowsCount / THREADS_COUNT;
    int rowsRest = rowsCount % THREADS_COUNT;

    Vector y = new Vector(rowsCount);

    for (int thr = 0, rowsStart = 0; thr < THREADS_COUNT;
      thr++) {
      int rowsInThread;
      if (thr < rowsRest) {
        rowsInThread = rowsPerThread + 1;
      } else {
        rowsInThread = rowsPerThread;
      }
      threads[thr] = new MultiplyThread(x, a, y, rowsStart,
        rowsInThread);
      threads[thr].start();
      rowsStart += rowsInThread;
    }
    for (MultiplyThread thr : threads) {
      try {
        thr.join();
      } catch (InterruptedException ex) {
        System.err.println(ex);
        System.exit(1);
      }
    }
    return y;

  }

  /**
   * Vector's class.
   */
  public static class Vector {

    private int n;
    private Element[] els;

    /**
     * Constructor
     *
     * @param n size of the vector
     */
    public Vector(int n) {
      this.n = n;
      els = new Element[n];
    }

    /**
     * @return size of the vector
     */
    public int n() {
      return n;
    }

    /**
     * Getter - gives the access to requested element of the array. It
     * implements tactic of lazy initialization.
     *
     * @param index index of the requested element
     * @return requested element
     */
    public Element index(int index) {
      if (els[index] == null) {
        els[index] = new Element();
      }
      return els[index];
    }

    /**
     * @return Max array's value.
     */
    public float max() {
      return Collections.max(Arrays.asList(els)).val();
    }

    /**
     * toString() implementation
     *
     * @return String representing specific array
     */
    @Override
    public String toString() {
      String string = Arrays.toString(els).
        replace(", ", "\t").replace("null", "0").
        replaceAll("[\\[\\]]", "");
      return string;
    }

    /**
     * Element of the array. Hold the value of the specific element.
     */
    public static class Element
      implements Comparable<Element> {

      private Float val;

      /**
       * Constructor
       */
      public Element() {
        val = new Float(0);
      }

      /**
       * Getter
       *
       * @return value of the element
       */
      float val() {
        return val;
      }

      /**
       * Setter
       *
       * @param val new value of the element
       */
      void val(float val) {
        this.val = val;
      }

      /**
       * Adder. Ensures synchronized addition - just in case.
       *
       * @param component
       */
      void add(float component) {
        synchronized (this.val) {
          this.val += component;
        }
      }

      /**
       * toString() implementation
       *
       * @return String representing specific element
       */
      @Override
      public String toString() {
        return String.format("%.2f", val);
      }

      /**
       * compareTo() implementation
       *
       * @param t compared element
       * @return 1: this is bigger, 0: those are equal, -1: this is
       * smaller
       */
      @Override
      public int compareTo(Element t) {
        float comparator = val() - t.val();
        if (comparator == 0) {
          return 0;
        } else {
          return (comparator < 0) ? (-1) : 1;
        }
      }
    }
  }

  /**
   * Matrix class
   */
  public static class Matrix {

    int n;
    Row[] rows;

    /**
     * Constructor
     *
     * @param n size of the matrix
     */
    public Matrix(int n) {
      this.n = n;
      rows = new Row[n];
    }

    /**
     * Getter - Gives access to requested row of the matrix. It
     * implements tactic of lazy initialization.
     *
     * @param i index of the requested row
     * @return requested row
     */
    public Row row(int i) {
      if (rows[i] == null) {
        rows[i] = new Row(n);
      }
      return rows[i];
    }

    /**
     * Getter
     *
     * @return size of the matrix
     */
    public int n() {
      return n;
    }

    /**
     * toString() implementation
     *
     * @return String representing specific element
     */
    @Override
    public String toString() {
      String string = Arrays.toString(rows).
        replace(", ", "\n").replaceAll("[\\[\\]]", "");
      return string;
    }

    /**
     * Class representing row.
     */
    public static class Row
      extends Vector {

      /**
       * Contructor
       *
       * @param n size of the row
       */
      public Row(int n) {
        super(n);
      }

      /**
       *
       * @param j index of the requested column
       * @return row's element from requested column
       */
      public Element col(int j) {
        return index(j);
      }
    }
  }
}

/**
 * Single thread of the matrix and vector multiplication.
 *
 */
class MultiplyThread
  extends Thread {

  Multiply.Vector x;
  Multiply.Matrix a;
  Multiply.Vector y;
  int jStart;
  int jEnd;

  /**
   * Thread's constructor.
   *
   * @param x
   * @param a
   * @param y
   * @param jStart
   * @param jCount
   */
  public MultiplyThread(Multiply.Vector x, Multiply.Matrix a,
    Multiply.Vector y, int jStart, int jCount) {
    this.x = x;
    this.y = y;
    this.a = a;
    this.jStart = jStart;
    this.jEnd = jStart + jCount;
  }

  /**
   * Main thread's method to be executed paralelly.
   */
  @Override
  public void run() {
    for (int j = 0; j < x.n(); j++) {
      float x_j = x.index(j).val();
      for (int i = jStart; i < jEnd; i++) {
        y.index(i).add(x_j * a.row(i).col(j).val());
      }
    }
  }
}