package org.example.Database;

import InterviewObject.Interview;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WriteSQLQueries {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/q2data";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "Acc0@user";

    public JFreeChart MaxInterviewsQuery() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            String query = "SELECT teamName , COUNT(*) as count from interviews " +
                    "WHERE MONTH(imonth) IN (10, 11) AND YEAR(imonth) = 2023 " +
                    "GROUP BY teamName " +
                    "ORDER BY count DESC " +
                    " Limit 1";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    String category = set.getString("teamName");
                    int value = set.getInt("count");
                    dataset.addValue(value, "Records", category);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "Team with maximum Interviews in Oct'23 and Nov'23",
                "Team",
                "Interviews Count",
                dataset
        );

        return chart;
    }

    public JFreeChart MinInterviewsQuery() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            String query = "SELECT teamName , COUNT(*) as count from interviews " +
                    "WHERE MONTH(imonth) IN (10, 11) AND YEAR(imonth) = 2023 " +
                    "GROUP BY teamName " +
                    "ORDER BY count" +
                    " Limit 1";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet set = statement.executeQuery()) {
                while (set.next()) {
                    String category = set.getString("teamName");
                    int value = set.getInt("count");
                    dataset.addValue(value, "Records", category);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "Team with minimum Interviews in Oct'23 and Nov'23",
                "Team",
                "Interviews Count",
                dataset
        );
        return chart;
    }



    public JFreeChart getTop3Skills() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            // Create the view if it doesn't exist
            createTopSkillsView(connection);

            // Execute the SELECT statement on the view
            String sql = "SELECT skill, skill_count FROM top_skills_view ORDER BY skill_count DESC LIMIT 3";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                // Execute the query
                ResultSet resultSet = preparedStatement.executeQuery();

                // Process the result
                while (resultSet.next()) {
                    // Access columns by name or index
                    String skill = resultSet.getString("skill");
                    int skillCount = resultSet.getInt("skill_count");
                    // Process the values as needed
                    dataset.addValue(skillCount, "Records", skill);
                    System.out.println("Skill: " + skill + " Count: " + skillCount);
                }
            }
            return ChartFactory.createBarChart("Top 3 skills in the months October and November", "Skill", "Skill Count", dataset, PlotOrientation.VERTICAL, true, true, false);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }


    }

    static void createTopSkillsView(Connection connection) throws SQLException {
        // Create the view
        String createViewSql = "CREATE VIEW top_skills_view AS " +
                "SELECT skill, COUNT(*) as skill_count " +
                "FROM interviews " +
                "WHERE MONTH(imonth) IN (10, 11) AND YEAR(imonth) = 2023 " +
                "GROUP BY skill";

        try (PreparedStatement createViewStatement = connection.prepareStatement(createViewSql)) {
            createViewStatement.execute();
        }
    }

    public JFreeChart getTop3killsForPeakTime() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            // Create the view if it doesn't exist
            createPeakTimeInterviewsView(connection);

            // Execute the SELECT statement on the view
            String sql = "SELECT skill, skill_count FROM peak_time_interviews ORDER BY skill_count DESC LIMIT 3";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                // Execute the query
                ResultSet resultSet = preparedStatement.executeQuery();

                // Process the result
                while (resultSet.next()) {
                    // Access columns by name or index
                    String skill = resultSet.getString("skill");
                    int skillCount = resultSet.getInt("skill_count");
                    dataset.addValue(skillCount, "Records", skill);
                    // Process the values as needed
                    System.out.println("Skill: " + skill + " Count: " + skillCount);
                }
            }
            return ChartFactory.createBarChart("Top 3 skills in Peak Time BETWEEN (9 AND 17 )", "Skill", "Skill Count", dataset, PlotOrientation.VERTICAL, true, true, false);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    static void createPeakTimeInterviewsView(Connection connection) throws SQLException {
        // Create the view for peak time interviews
        String createViewSql = "CREATE VIEW peak_time_interviews AS " +
                "SELECT skill, COUNT(*) as skill_count " +
                "FROM interviews " +
                "WHERE EXTRACT(HOUR FROM itime) BETWEEN 9 AND 17 " + // Adjust peak time hours
                "GROUP BY skill";

        try (PreparedStatement createViewStatement = connection.prepareStatement(createViewSql)) {
            createViewStatement.execute();
        }
    }

     public JFreeChart getTop3Panels(List<Interview> interviewList) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
        Map<String, Integer> result = interviewList.stream().filter(emp -> dateFormat.format(emp.getMonth()) != null && dateFormat.format(emp.getMonth()).equals("2023-10-01 00:00:00") || dateFormat.format(emp.getMonth()) != null && dateFormat.format(emp.getMonth()).equals("2023-11-01 00:00:00")).collect(Collectors.groupingBy(record -> record.getPanelName(), Collectors.summingInt(r -> 1)));

        List<Map.Entry<String,Integer>> top3Panels= result.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).limit(3).collect(Collectors.toList());
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        top3Panels.forEach(entry -> dataset.addValue(entry.getValue(), "Interviews", entry.getKey()));

        return ChartFactory.createBarChart("Top 3 panels in October and November 2023", "Panel", "Interview Count", dataset, PlotOrientation.VERTICAL, true, true, false);
    }
}