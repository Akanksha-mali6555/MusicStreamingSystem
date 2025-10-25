package gui;
import java.sql.*;
import java.io.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class sample{
    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/musicstreamingdb1", "root", "root123");

        PreparedStatement ps = conn.prepareStatement("SELECT Image_Path FROM artist WHERE Name = ?");
        ps.setString(1, "Arijit Singh");
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            byte[] imgBytes = rs.getBytes(1);
            if (imgBytes != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(imgBytes);
                BufferedImage img = ImageIO.read(bis);
                JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(img)));
            } else {
                System.out.println("No image bytes found.");
            }
        }
        rs.close();
        ps.close();
        conn.close();
    }
}

