/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.catboardingsystem;

/**
 *
 * @author LENOVO GAMING 3
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

 private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Pembayaran.class.getName());
    private static Connection mysqlconfig;
    
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
    
    public Pembayaran() {
        initComponents();
        loadComboKucing();
        loadTable();
        kosong();
        jTextField7.setEditable(false);
        jTextField8.setText("Lunas");
    }
    private void loadComboKucing() {
        try {
            String sql = "SELECT b.id_booking, k.nama as nama_kucing " +
                         "FROM pemesanan b " +
                         "JOIN kucing k ON b.id_kucing = k.id_kucing " +
                         "WHERE b.status = 'Check In' AND b.status_pembayaran = 0";
            Connection conn = configDB();
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            jComboBox1.removeAllItems();
            while (rs.next()) {
                int idBooking = rs.getInt("id_booking");
                String namaKucing = rs.getString("nama_kucing");
                jComboBox1.addItem(idBooking + " - " + namaKucing);
            }
            if (jComboBox1.getItemCount() == 0) {
                jComboBox1.addItem("Tidak ada data");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load data kucing: " + e.getMessage());
        }
    }
    
    // Saat memilih ID Kucing, hitung total biaya dari booking
    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {
        String selected = (String) jComboBox1.getSelectedItem();
        if (selected == null || selected.equals("Tidak ada data")) {
            jTextField7.setText("");
            return;
        }
        int idBooking = Integer.parseInt(selected.split(" - ")[0]);
        try {
            String sql = "SELECT total_biaya FROM pemesanan WHERE id_booking = ?";
            Connection conn = configDB();
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, idBooking);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                double total = rs.getDouble("total_biaya");
                jTextField7.setText(String.format("%,.0f", total));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mengambil total: " + e.getMessage());
        }
    }
    
    // Tombol Bayar
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
        String selected = (String) jComboBox1.getSelectedItem();
        if (selected == null || selected.equals("Tidak ada data")) {
            JOptionPane.showMessageDialog(this, "Pilih ID Kucing terlebih dahulu!");
            return;
        }
        int idBooking = Integer.parseInt(selected.split(" - ")[0]);
        String status = jTextField8.getText().trim();
        double total;
        try {
            total = Double.parseDouble(jTextField7.getText().replace(",", ""));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Total tidak valid!");
            return;
        }
        
        if (status.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Status harus diisi!");
            return;
        }
        
        try {
            // Update status pembayaran di tabel pemesanan
            String updateBooking = "UPDATE pemesanan SET status_pembayaran = 1 WHERE id_booking = ?";
            PreparedStatement pst1 = configDB().prepareStatement(updateBooking);
            pst1.setInt(1, idBooking);
            pst1.executeUpdate();
            
            // Insert ke tabel pembayaran
            String insertPembayaran = "INSERT INTO pembayaran (id_booking, total, status, tanggal_bayar) VALUES (?, ?, ?, ?)";
            PreparedStatement pst2 = configDB().prepareStatement(insertPembayaran);
            pst2.setInt(1, idBooking);
            pst2.setDouble(2, total);
            pst2.setString(3, status);
            pst2.setDate(4, java.sql.Date.valueOf(LocalDate.now()));
            pst2.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Pembayaran berhasil!");
            loadComboKucing();
            loadTable();
            kosong();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memproses pembayaran: " + e.getMessage());
        }
    }
    
    // Tombol Kembali
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        StaffPanel sp = new StaffPanel();
        sp.setVisible(true);
        this.dispose();
    }
    
    private void loadTable() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("ID");
        model.addColumn("Kucing");
        model.addColumn("Kamar");
        model.addColumn("Entry");
        model.addColumn("Exit");
        model.addColumn("Status");
        
        try {
            String sql = "SELECT b.id_booking, k.nama as nama_kucing, r.tipe as kamar, " +
                         "b.tanggal_masuk, b.tanggal_keluar, " +
                         "CASE WHEN b.status_pembayaran = 1 THEN 'Lunas' ELSE 'Belum Lunas' END as status " +
                         "FROM pemesanan b " +
                         "JOIN kucing k ON b.id_kucing = k.id_kucing " +
                         "JOIN ruangan r ON b.id_kamar = r.id_kamar " +
                         "ORDER BY b.id_booking DESC";
            Connection conn = configDB();
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_booking"),
                    rs.getString("nama_kucing"),
                    rs.getString("kamar"),
                    rs.getDate("tanggal_masuk"),
                    rs.getDate("tanggal_keluar"),
                    rs.getString("status")
                });
            }
            jTable1.setModel(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal load tabel: " + e.getMessage());
        }
    }
    
    private void kosong() {
        if (jComboBox1.getItemCount() > 0) {
            jComboBox1.setSelectedIndex(0);
        }
        jTextField7.setText("");
        jTextField8.setText("Lunas");
    }
        public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new Pembayaran().setVisible(true));
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jLabel4 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTextField7 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel4.setText("Total                     :");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addActionListener(this::jComboBox1ActionPerformed);

        jLabel5.setText("Status                    :");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Kucing", "Kamar", "Entry", "Exit", "Status"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("PEMBAYARAN");

        jLabel3.setText("ID Kucing             :");

        jButton1.setText("Kembali");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jButton5.setText("Bayar");
        jButton5.addActionListener(this::jButton5ActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(184, 184, 184)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(97, 97, 97)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING))
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField7)
                            .addComponent(jTextField8)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(56, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addComponent(jButton5)
                .addGap(56, 56, 56))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1)
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(51, 51, 51)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton5))
                .addContainerGap(63, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                        

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // TODO add your handling code here:
    }                                        

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // TODO add your handling code here:
    }                                        

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {                                           
            // TODO add your handling code here:
    }                                          

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
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
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Pembayaran().setVisible(true));
    }

    // Variables declaration - do not modify                     
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton5;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    // End of variables declaration                   
}
