package AddressBook_JDBC;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

public class AddressBookDBService {

	private static AddressBookDBService addressBookDBService;
	private PreparedStatement addressBookPreparedStatement;
	private List<AddressBookData> addressBookData;

	private AddressBookDBService() {
	}

	private Connection getConnection() throws SQLException {
		String jdbcURL = "jdbc:mysql://localhost:3306/addressbookservice?useSSL=false";
		String username = "root";
		String password = "Engineer@21";
		Connection con;
		System.out.println("Connecting to database:" + jdbcURL);
		con = DriverManager.getConnection(jdbcURL, username, password);
		System.out.println("Connection is successful:" + con);
		return con;

	}

	public static AddressBookDBService getInstance() {
		if (addressBookDBService == null)
			addressBookDBService = new AddressBookDBService();
		return addressBookDBService;
	}

	public List<AddressBookData> readData() throws AddressBookException {
		String query = null;
		query = "select * from addressbook";
		return getAddressBookDataUsingDB(query);
	}

	private List<AddressBookData> getAddressBookDataUsingDB(String sql) throws AddressBookException {
		List<AddressBookData> addressBookData = new ArrayList<>();
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			addressBookData = this.getAddressBookDetails(resultSet);
		} catch (SQLException e) {
			throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.DATABASE_EXCEPTION);
		}
		return addressBookData;
	}
	private void prepareAddressBookStatement() throws AddressBookException {
		try {
			Connection connection = this.getConnection();
			String query = "select * from addressbook where First_Name = ?";
			addressBookPreparedStatement = connection.prepareStatement(query);
		} catch (SQLException e) {
			throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.DATABASE_EXCEPTION);
		}
	
	}
	private List<AddressBookData> getAddressBookDetails(ResultSet resultSet) throws AddressBookException {
		List<AddressBookData> addressBookData = new ArrayList<>();
		try {
			while (resultSet.next()) {
				String firstName = resultSet.getString("First_Name");
				String lastName = resultSet.getString("Last_Name");
				String address = resultSet.getString("Address");
				String city = resultSet.getString("City");
				String state = resultSet.getString("State");
				String zip = resultSet.getString("Zip");
				String phoneNo = resultSet.getString("Phone_Number");
				String email = resultSet.getString("Email");
				addressBookData.add(new AddressBookData(firstName, lastName, address, city, state,  phoneNo, email));
			}
		} catch (SQLException e) {
			throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.DATABASE_EXCEPTION);
		}
		return addressBookData;
	}
	public int updateAddressBookData(String firstname, String address) throws AddressBookException {
		try (Connection connection = this.getConnection()) {
			String query = String.format("update addressbook set Address = '%s' where First_Name = '%s';", address,
					firstname);
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			return preparedStatement.executeUpdate(query);
		} catch (SQLException e) {
			throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.CONNECTION_FAILED);
		}
	}

	public List<AddressBookData> getAddressBookData(String firstname) throws AddressBookException {
		if (this.addressBookPreparedStatement == null)
			this.prepareAddressBookStatement();
		try {
			addressBookPreparedStatement.setString(1, firstname);
			ResultSet resultSet = addressBookPreparedStatement.executeQuery();
			addressBookData = this.getAddressBookDetails(resultSet);
		} catch (SQLException e) {
			throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.CONNECTION_FAILED);
		}
		System.out.println(addressBookData);
		return addressBookData;
	}
	public List<AddressBookData> readData(LocalDate start, LocalDate end) throws AddressBookException {
		String query = null;
		if (start != null)
			query = String.format("select * from addressbook where Date between '%s' and '%s';", start, end);
		if (start == null)
			query = "select * from addressbook";
		List<AddressBookData> addressBookList = new ArrayList<>();
		try (Connection con = this.getConnection();) {
			Statement statement = con.createStatement();
			ResultSet rs = statement.executeQuery(query);
			addressBookList = this.getAddressBookDetails(rs);
		} catch (SQLException e) {
			throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.DATABASE_EXCEPTION);
		}
		return addressBookList;
	}
	public int readDataBasedOnCity(String total, String city) throws AddressBookException {
		int count = 0;
		String query = String.format("select  state='%s' from addressbook where City = '%s' group by City;"
				, total, city);
		try (Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			resultSet.next();
			count = resultSet.getInt(1);
		} catch (SQLException e) {
			throw new AddressBookException(e.getMessage(), AddressBookException.ExceptionType.DATABASE_EXCEPTION);
		}
		return count;
	}


}