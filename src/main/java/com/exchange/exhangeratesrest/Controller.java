package com.exchange.exhangeratesrest;

import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


@RestController
public class Controller {
    private final String url = "jdbc:postgresql://hattie.db.elephantsql.com:5432/allcptwz";
    private final String user = "allcptwz";
    private final String password = getDBPassword();
    private final ZoneId zoneId = ZoneId.systemDefault();
    private Connection conn = connect();
    private ExchangeRates er = new ExchangeRates();

    @GetMapping("/last10days")
    public Map<String, Map<String, Double>> getJson(){
        System.out.println();
        return getLatest10Rates();
    }
    @GetMapping("/rates-by-date")
    public Map<String, Double> ratesByDate(@RequestParam(value = "date", defaultValue = "") String date){
        if ("".equals(date) || "".equals(date.strip())){
            date = LocalDate.now().toString();
        }

        Map<String, Double> output = getExchangeRatesByDate(date);
        if (output.isEmpty()){
            output.put("success", null);
        }else{
            output.put("success", 1.0);
        }

        return output;
    }
    @GetMapping("/rates-change")
    public Map<String, Double> exhangeRatesChange(){
        Map<String, Double> output = didExchangeRatesChange();

        if (output.isEmpty()){
            output.put("success", null);
        }else{
            output.put("success", 1.0);
        }

        return output;

    }

    public Map<String, Double> getExchangeRatesByDate(String date){
        Map<String, Double> result = new HashMap<>();
        String sql = "SELECT * FROM exchange_rates WHERE add_date=?";
        LocalDate localDate = LocalDate.parse(date);

        try {
            PreparedStatement st = conn.prepareStatement(sql);
            st.setDate(1, new Date(localDate.atStartOfDay(zoneId).toInstant().toEpochMilli()));
            ResultSet resultSet = st.executeQuery();

            while (resultSet.next()){

                Double usd = resultSet.getDouble("usd");
                Double eur = resultSet.getDouble("eur");
                Double rub = resultSet.getDouble("rub");

                result.put("USD", usd);
                result.put("EUR", eur);
                result.put("RUB", rub);            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }


    public Map<String, Double> didExchangeRatesChange(){
        Map<String, Double> result = new TreeMap<>();
        Double threshold = 0.1;
        String sql = "SELECT * \n" +
                "FROM    (SELECT * FROM exchange_rates\n" +
                "        ORDER BY add_date DESC\n" +
                "        LIMIT 2) AS table1\n" +
                "ORDER BY table1.add_date ASC;\n";

        try {
            PreparedStatement st = conn.prepareStatement(sql);
            ResultSet resultSet = st.executeQuery();

            while (resultSet.next()){
                Date date = resultSet.getDate("add_date");

                Double usd = resultSet.getDouble("usd");
                if (result.containsKey("USD")){
                    Double changeCoef = Math.abs(usd - result.get("USD"))/result.get("USD");
                    result.put("USD", (changeCoef > threshold) ? changeCoef : null);
                }else{
                    result.put("USD", usd);
                }

                Double eur = resultSet.getDouble("eur");
                if (result.containsKey("EUR")){
                    Double changeCoef = Math.abs(eur - result.get("EUR"))/result.get("EUR");
                    result.put("EUR", (changeCoef > threshold) ? changeCoef : null);
                }else{
                    result.put("EUR", eur);
                }

                Double rub = resultSet.getDouble("rub");
                if (result.containsKey("RUB")){
                    Double changeCoef = Math.abs(rub - result.get("RUB"))/result.get("RUB");
                    result.put("RUB", (changeCoef > threshold) ? changeCoef : null);
                }else{
                    result.put("RUB", rub);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }



        return result;
    }

    public void updateDB(){
        Map<String, Map<String, Double>> response = er.getLastNDaysRates(2);
        for (Map.Entry<String, Map<String, Double>> entry: response.entrySet()){
            addNewRow(entry.getKey(), entry.getValue());
        }
    }

    public Map<String, Map<String, Double>> getLatest10Rates(){
        Map<String, Map<String, Double>> result = new TreeMap<>();

        updateDB();

        String sql = "SELECT * \n" +
                "FROM    (SELECT * FROM exchange_rates\n" +
                "        ORDER BY add_date DESC\n" +
                "        LIMIT 10) AS table1\n" +
                "ORDER BY table1.add_date ASC;\n";

        try {
            PreparedStatement st = conn.prepareStatement(sql);
            ResultSet resultSet = st.executeQuery();

            while (resultSet.next()){
                Date date = resultSet.getDate("add_date");
                Double usd = resultSet.getDouble("usd");
                Double eur = resultSet.getDouble("eur");
                Double rub = resultSet.getDouble("rub");
                Map<String, Double> ratesMap = new HashMap<>();
                ratesMap.put("USD", usd);
                ratesMap.put("EUR", eur);
                ratesMap.put("RUB", rub);

                result.put(date.toLocalDate().toString(), ratesMap);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return  result;
    }

    public boolean addNewRow(String date, Map<String, Double> rates){
        boolean doesRowExist = false;
        try {
            PreparedStatement st = conn.prepareStatement("SELECT * FROM exchange_rates WHERE add_date = ?");
            LocalDate localDate = LocalDate.parse(date);
            System.out.println(localDate.getYear() + ", " + localDate.getMonthValue()+ ", " + localDate.getDayOfMonth());

            st.setDate(1,
                    new Date(localDate.atStartOfDay(zoneId).toInstant().toEpochMilli()));
            System.out.println(st.toString());
            ResultSet resultSet = st.executeQuery();
            doesRowExist = resultSet.next();
            if (doesRowExist)
                System.out.println(resultSet.getDate("add_date"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!doesRowExist){
            String sql = "INSERT INTO exchange_rates(add_date, usd, eur, rub) "
                    + "VALUES (?, ?, ?, ?)";
            LocalDate localDate = LocalDate.parse(date);


            try {
                PreparedStatement prepStatement = conn.prepareStatement(sql);
                prepStatement.setDate(1, new Date(localDate.atStartOfDay(zoneId).toInstant().toEpochMilli()));
                prepStatement.setDouble(2, rates.get("USD"));
                prepStatement.setDouble(3, rates.get("EUR"));
                prepStatement.setDouble(4, rates.get("RUB"));
                prepStatement.executeUpdate();
                prepStatement.close();
                System.out.println(prepStatement.toString() + " EXECUTED");
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return true;
        }

        return false;
    }

    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    private String getDBPassword(){
        Dotenv dotenv = null;
        dotenv = Dotenv.configure().load();
        return dotenv.get("DB_PASSWORD");
    }


}
