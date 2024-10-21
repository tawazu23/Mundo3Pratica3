package cadastro.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import cadastro.model.util.ConectorBD;
import cadastrobd.model.PessoaFisica;

public class PessoaFisicaDAO {

    public ConectorBD cnx = new ConectorBD();

    public PessoaFisica getPessoa(Integer id) throws SQLException {
        String sql = "SELECT pf.idPessoa, pf.cpf, p.nome, p.endereco, p.cidade, p.estado, p.telefone, p.email "
                + "FROM PessoaFisica pf "
                + "INNER JOIN Pessoa p ON pf.idPessoa = p.idPessoa "
                + "WHERE pf.idPessoa = ?";
        try (Connection con = cnx.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PessoaFisica p = new PessoaFisica(
                            rs.getString("cpf"),
                            rs.getInt("idPessoa"),
                            rs.getString("nome"),
                            rs.getString("endereco"),
                            rs.getString("cidade"),
                            rs.getString("estado"),
                            rs.getString("telefone"),
                            rs.getString("email")
                    );
                    return p;
                }
            }
        }
        return null;
    }

    public ArrayList<PessoaFisica> getPessoas() throws SQLException {
        ArrayList<PessoaFisica> list = new ArrayList<>();
        String sql = "SELECT pf.idPessoa, pf.cpf, p.nome, p.endereco, p.cidade, p.estado, p.telefone, p.email "
                + "FROM PessoaFisica pf "
                + "INNER JOIN Pessoa p ON pf.idPessoa = p.idPessoa";
        try (Connection con = cnx.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PessoaFisica p = new PessoaFisica(
                        rs.getString("cpf"),
                        rs.getInt("idPessoa"),
                        rs.getString("nome"),
                        rs.getString("endereco"),
                        rs.getString("cidade"),
                        rs.getString("estado"),
                        rs.getString("telefone"),
                        rs.getString("email")
                );
                list.add(p);
            }
        }
        return list;
    }


    public int incluir(PessoaFisica p) throws SQLException {
        if (p.getNome() == null || p.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O campo 'nome' não pode ser nulo ou vazio.");
        }

        String sqlInsertPessoa = "INSERT INTO Pessoa (nome, endereco, cidade, estado, telefone, email) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlInsertPessoaFisica = "INSERT INTO PessoaFisica (idPessoa, cpf) VALUES (?, ?)";

        try (Connection con = cnx.getConnection();
             PreparedStatement stmtPessoa = con.prepareStatement(sqlInsertPessoa, Statement.RETURN_GENERATED_KEYS)) {

            stmtPessoa.setString(1, p.getNome());
            stmtPessoa.setString(2, p.getEndereco());
            stmtPessoa.setString(3, p.getCidade());
            stmtPessoa.setString(4, p.getEstado());
            stmtPessoa.setString(5, p.getTelefone());
            stmtPessoa.setString(6, p.getEmail());
            int affectedRows = stmtPessoa.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmtPessoa.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idNovaPessoa = generatedKeys.getInt(1);
                    p.setId(idNovaPessoa);
                    try (PreparedStatement stmtPessoaFisica = con.prepareStatement(sqlInsertPessoaFisica)) {
                        stmtPessoaFisica.setInt(1, idNovaPessoa);
                        stmtPessoaFisica.setString(2, p.getCpf());
                        stmtPessoaFisica.executeUpdate();
                    }
                    return idNovaPessoa;
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }


    public void alterar(PessoaFisica p) throws SQLException {
        String sqlUpdatePessoa = "UPDATE Pessoa SET nome = ?, endereco = ?, cidade = ?, estado = ?, telefone = ?, email = ? WHERE idPessoa = ?;";
        String sqlUpdatePessoaFisica = "UPDATE PessoaFisica SET cpf = ? WHERE idPessoa = ?;";
        try (Connection con = cnx.getConnection();
             PreparedStatement stmtPessoa = con.prepareStatement(sqlUpdatePessoa);
             PreparedStatement stmtPessoaFisica = con.prepareStatement(sqlUpdatePessoaFisica)) {


            stmtPessoa.setString(1, p.getNome());
            stmtPessoa.setString(2, p.getEndereco());
            stmtPessoa.setString(3, p.getCidade());
            stmtPessoa.setString(4, p.getEstado());
            stmtPessoa.setString(5, p.getTelefone());
            stmtPessoa.setString(6, p.getEmail());
            stmtPessoa.setInt(7, p.getId());
            stmtPessoa.executeUpdate();


            stmtPessoaFisica.setString(1, p.getCpf());
            stmtPessoaFisica.setInt(2, p.getId());
            stmtPessoaFisica.executeUpdate();
        }
    }

    public void excluir(PessoaFisica p) throws SQLException {
        String sqlDeletePessoaFisica = "DELETE FROM PessoaFisica WHERE idPessoa = ?;";
        String sqlDeletePessoa = "DELETE FROM Pessoa WHERE idPessoa = ?;";
        try (Connection con = cnx.getConnection();
             PreparedStatement stmtPessoaFisica = con.prepareStatement(sqlDeletePessoaFisica);
             PreparedStatement stmtPessoa = con.prepareStatement(sqlDeletePessoa)) {


            stmtPessoaFisica.setInt(1, p.getId());
            stmtPessoaFisica.executeUpdate();


            stmtPessoa.setInt(1, p.getId());
            stmtPessoa.executeUpdate();
        }
    }

    public void close() throws SQLException {
        cnx.close();
    }
}