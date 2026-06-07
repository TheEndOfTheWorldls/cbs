package com.mycompany.catboardingsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author LENOVO GAMING 3
 */
public class Ruangan extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Ruangan.class.getName());
    private static Connection mysqlconfig;
    private String id; // menyimpan id_kamar asli (integer)

    public static Connection configDB() throws SQLException {
        try {
            String url = "jdbc:mysql://localhost:3306/catboardingsystem";
            String user = "root";
            String pass = "";

            Class.forName("com.mysql.cj.jdbc.Driver");
            mysqlconfig = DriverManager.getConnection(url, user, pass);

        } catch (Exception e) {
            System.err.println("koneksi gagal " + e.getMessage());
        }
        return mysqlconfig;
    }
        
    public Ruangan() {
        initComponents();
        // Set ID Kamar tidak bisa diedit
        jTextField1.setEditable(false);
        load_table();
        kosong();
    }

    // Method untuk memisahkan harga dari string tipe (contoh: "Standar-Rp.500.000" -> 500000)
    private double getHargaFromTipe(String tipeWithHarga) {
        try {
            int idxRp = tipeWithHarga.indexOf("Rp.");
            if (idxRp >= 0) {
                String hargaStr = tipeWithHarga.substring(idxRp + 3).replace(".", "");
                return Double.parseDouble(hargaStr);
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    // Method untuk mendapatkan tipe murni (tanpa harga)
    private String getTipeMurni(String tipeWithHarga) {
        int idx = tipeWithHarga.indexOf("-Rp");
        if (idx > 0) {
            return tipeWithHarga.substring(0, idx);
        }
        return tipeWithHarga;
    }

    private void kosong() {
        jTextField1.setText("");
        jTextField3.setText("");
        jComboBox5.setSelectedIndex(0);
        jCheckBox1.setSelected(true);
        id = null;
    }

    private void load_table() {
        DefaultTableModel model = new DefaultTableModel();

        model.addColumn("ID Kamar");
        model.addColumn("Tipe");
        model.addColumn("Kapasitas");
        model.addColumn("Harga");
        model.addColumn("Status");

        try {
            String sql = "SELECT * FROM ruangan";

            Connection conn = configDB();
            java.sql.Statement stm = conn.createStatement();
            java.sql.ResultSet res = stm.executeQuery(sql);

            while (res.next()) {
                String status = res.getBoolean("tersedia") ? "Tersedia" : "Tidak Tersedia";
                model.addRow(new Object[]{
                    res.getInt("id_kamar"),
                    res.getString("tipe"),
                    res.getInt("kapasitas"),
                    res.getDouble("harga_per_hari"),
                    status
                });
            }

            jTable1.setModel(model);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load data: " + e.getMessage());
        }
    }

    // Tombol Simpan
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            String tipeWithHarga = jComboBox5.getSelectedItem().toString();
            String tipeMurni = getTipeMurni(tipeWithHarga);
            double harga = getHargaFromTipe(tipeWithHarga);
            int kapasitas = Integer.parseInt(jTextField3.getText());
            boolean tersedia = jCheckBox1.isSelected();

            String sql = "INSERT INTO ruangan(tipe, kapasitas, harga_per_hari, tersedia) VALUES ('"
                    + tipeMurni + "',"
                    + kapasitas + ","
                    + harga + ","
                    + (tersedia ? 1 : 0) + ")";

            Connection conn = configDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.execute();

            JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        load_table();
        kosong();
    }

    // Tombol Edit
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        if (id == null || id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data dari tabel terlebih dahulu!");
            return;
        }
        try {
            String tipeWithHarga = jComboBox5.getSelectedItem().toString();
            String tipeMurni = getTipeMurni(tipeWithHarga);
            double harga = getHargaFromTipe(tipeWithHarga);
            int kapasitas = Integer.parseInt(jTextField3.getText());
            boolean tersedia = jCheckBox1.isSelected();

            String sql = "UPDATE ruangan SET "
                    + "tipe='" + tipeMurni + "', "
                    + "kapasitas=" + kapasitas + ", "
                    + "harga_per_hari=" + harga + ", "
                    + "tersedia=" + (tersedia ? 1 : 0) + " "
                    + "WHERE id_kamar=" + id;

            Connection conn = configDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil diubah");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        load_table();
        kosong();
    }

    // Tombol Hapus
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
        if (id == null || id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data dari tabel terlebih dahulu!");
            return;
        }
        try {
            String sql = "DELETE FROM ruangan WHERE id_kamar=" + id;

            Connection conn = configDB();
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.execute();

            JOptionPane.showMessageDialog(this, "Data berhasil dihapus");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        load_table();
        kosong();
    }

    // Tombol Reset
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
        kosong();
        load_table();
    }

    // Tombol Cari (berdasarkan ID Kamar)
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
        String keyword = jTextField1.getText().trim();
        if (keyword.isEmpty()) {
            load_table();
            return;
        }
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID Kamar");
        model.addColumn("Tipe");
        model.addColumn("Kapasitas");
        model.addColumn("Harga");
        model.addColumn("Status");

        try {
            String sql = "SELECT * FROM ruangan WHERE id_kamar = " + keyword;
            Connection conn = configDB();
            java.sql.Statement stm = conn.createStatement();
            java.sql.ResultSet res = stm.executeQuery(sql);

            while (res.next()) {
                String status = res.getBoolean("tersedia") ? "Tersedia" : "Tidak Tersedia";
                model.addRow(new Object[]{
                    res.getInt("id_kamar"),
                    res.getString("tipe"),
                    res.getInt("kapasitas"),
                    res.getDouble("harga_per_hari"),
                    status
                });
            }
            jTable1.setModel(model);
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Data tidak ditemukan");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencari: " + e.getMessage());
        }
    }

    // Event klik tabel
    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {
        int row = jTable1.getSelectedRow();
        if (row >= 0) {
            id = jTable1.getValueAt(row, 0).toString();
            jTextField1.setText(id);
            String tipe = jTable1.getValueAt(row, 1).toString();
            for (int i = 0; i < jComboBox5.getItemCount(); i++) {
                String item = jComboBox5.getItemAt(i);
                if (item.startsWith(tipe)) {
                    jComboBox5.setSelectedIndex(i);
                    break;
                }
            }
            jTextField3.setText(jTable1.getValueAt(row, 2).toString());
            String status = jTable1.getValueAt(row, 4).toString();
            jCheckBox1.setSelected(status.equals("Tersedia"));
        }
    }

    // ========== Method yang dihasilkan NetBeans - Jangan dihapus ==========
    // (Kode initComponents() sudah ada dari desain GUI Anda, saya tidak tulis ulang)
    // Pastikan di dalam initComponents() sudah terhubung event listener.
    // Jika belum, tambahkan di konstruktor setelah initComponents():
    // jButton1.addActionListener(this::jButton1ActionPerformed);
    // jButton2.addActionListener(this::jButton2ActionPerformed);
    // jButton3.addActionListener(this::jButton3ActionPerformed);
    // jButton4.addActionListener(this::jButton4ActionPerformed);
    // jButton5.addActionListener(this::jButton5ActionPerformed);
    // jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
    //     public void mouseClicked(java.awt.event.MouseEvent evt) {
    //         jTable1MouseClicked(evt);
    //     }
    // });
    // </editor-fold>

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new Ruangan().setVisible(true));
    }

    // Variables declaration - do not modify
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JComboBox<String> jComboBox5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField3;
    private java.awt.List list1;
    // End of variables declaration
}