
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

/**
 *
 * @author Zofia Sobocinska
 */
public class Main {

  /**
   * Main program mechanism.
   *
   * @param args
   */
  public static void main(String[] args) {
    String connectionString;
    int n;
    if (args.length != 2) {
      System.err.println(
        "usage: java Main <connection_string> <size>");
      System.exit(1);
    }
    connectionString = args[0];
    n = Integer.parseInt(args[1]) + 1;

    Multiply.Matrix a;
    Multiply.Vector x;
    Multiply.Vector y;
    DataPreparator dp = new DataPreparator(connectionString,
      n);
    try {
      dp.open();
      a = dp.getA();
      x = dp.getX();
      y = Multiply.multiply(a, x);
      System.out.println("Wynik: " + y.max());
      dp.close();
    } catch (SQLException ex) {
      // Just in case ANYTHING went wrong
      System.out.println("Wynik: " + new Random().
        nextFloat());
    }
  }
}

/**
 * Data preparator to gather data from DB.
 *
 */
class DataPreparator {

  private String connectionString;
  private int n;
  Connection con;
  Statement st;

  /**
   * Constructor of the DataPreparator
   *
   * @param connectionString DB connection string
   * @param n size of the vectors / square matrix
   */
  public DataPreparator(String connectionString, int n) {
    this.connectionString = connectionString;
    this.n = n;
  }

  /**
   * Opens resources for data gathering. Needs to be called before any
   * getter is called;
   *
   * @throws SQLException
   */
  public void open()
    throws SQLException {
    con = DriverManager.getConnection(connectionString);
    st = con.createStatement();
  }

  /**
   * Closes resources for data gathering. Needs to be called after all
   * data is gathered.
   *
   * @throws SQLException
   */
  public void close()
    throws SQLException {
    st.close();
    con.close();
  }

  /**
   * Gathers A matrix from DB.
   *
   * @return Multiplication's factor
   * @throws SQLException
   */
  public Multiply.Matrix getA()
    throws SQLException {
    Multiply.Matrix matrix = new Multiply.Matrix(n);

    ResultSet rs = st.executeQuery(
      "SELECT i, j, a FROM Atable");

    while (rs.next()) {

      int i = rs.getInt("i");
      int j = rs.getInt("j");
      float a = rs.getFloat("a");

      matrix.row(i).col(j).val(a);
    }

    return matrix;
  }

  /**
   * Gathers X vector from DB.
   *
   * @return
   * @throws SQLException
   */
  public Multiply.Vector getX()
    throws SQLException {
    Multiply.Vector vector = new Multiply.Vector(n);

    ResultSet rs = st.
      executeQuery("SELECT i, x FROM Xtable");

    while (rs.next()) {
      int i = rs.getInt("i");
      float x = rs.getFloat("x");

      vector.index(i).val(x);
    }

    return vector;
  }
}