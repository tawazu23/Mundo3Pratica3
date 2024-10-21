package cadastro.model;

import cadastrobd.model.PessoaJuridica;
import cadastro.model.util.ConectorBD;

import java.sql.*;
import java.util.ArrayList;

public class PessoaJuridicaDAO {

    ConectorBD cnx = new ConectorBD();

    public PessoaJuridica getPessoa(Integer id) throws SQLException {
        ResultSet rs = cnx.getSelect("select\n"
                + "	PessoaJuridica.idPessoa as id,\n"
                + "	PessoaJuridica.cnpj,\n"
                + "	p.nome,\n"
                + "	p.endereco,\n"
                + "	p.cidade,\n"
                + "	p.estado,\n"
                + "	p.telefone,\n"
                + "	p.email\n"
                + "	from PessoaJuridica\n"
                + "INNER JOIN Pessoa as p on PessoaJuridica.idPessoa = p.idPessoa\n"
                + "WHERE\n"
                + "	PessoaJuridica.idPessoa = " + id.toString());

        rs.next();
        PessoaJuridica p = new PessoaJuridica(
                rs.getString("cnpj"),
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("endereco"),
                rs.getString("cidade"),
                rs.getString("estado"),
                rs.getString("telefone"),
                rs.getString("email")
        );
        p.exibir();
        cnx.close();
        return p;
    }

    public ArrayList<PessoaJuridica> getPessoas() throws SQLException {
        ArrayList<PessoaJuridica> list = new ArrayList<PessoaJuridica>();

        ResultSet rs = cnx.getSelect("select\n"
                + "	PessoaJuridica.idPessoa as id,\n"
                + "	PessoaJuridica.cnpj,\n"
                + "	p.nome,\n"
                + "	p.endereco,\n"
                + "	p.cidade,\n"
                + "	p.estado,\n"
                + "	p.telefone,\n"
                + "	p.email\n"
                + "	from PessoaJuridica\n"
                + "INNER JOIN Pessoa as p on PessoaJuridica.idPessoa = p.idPessoa;");

        while (rs.next()) {
            PessoaJuridica p = new PessoaJuridica(
                    rs.getString("cnpj"),
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("endereco"),
                    rs.getString("cidade"),
                    rs.getString("estado"),
                    rs.getString("telefone"),
                    rs.getString("email")
            );
            list.add(p);
        }
        cnx.close();
        return list;
    }

    public int incluir(PessoaJuridica p) throws SQLException {
        String sqlInsertPessoa = "INSERT INTO Pessoa (nome, endereco, cidade, estado, telefone, email) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlInsertPessoaJuridica = "INSERT INTO PessoaJuridica (idPessoa, cnpj) VALUES (?, ?)";

        try (Connection con = cnx.getConnection();
             PreparedStatement stmtPessoa = con.prepareStatement(sqlInsertPessoa, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtPessoaJuridica = con.prepareStatement(sqlInsertPessoaJuridica)) {


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


                    stmtPessoaJuridica.setInt(1, idNovaPessoa);
                    stmtPessoaJuridica.setString(2, p.getCnpj());
                    stmtPessoaJuridica.executeUpdate();

                    return idNovaPessoa;
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }


    public void alterar(PessoaJuridica novaPessoa) throws SQLException {
        String sqlUpdatePessoaJuridica = String.format(
                "UPDATE PessoaJuridica SET cnpj = '%s' where PessoaJuridica.idPessoa  = %s;",
                novaPessoa.getCnpj(), novaPessoa.getId()
        );
        System.out.println(sqlUpdatePessoaJuridica);
        cnx.update(sqlUpdatePessoaJuridica);

        ResultSet rs = cnx.getSelect("SELECT pj.idpessoa FROM PessoaJuridica pj WHERE idPessoa = " + novaPessoa.getId() + ";");
        if (rs.next()) {
            int id_pessoaAssociadaA_PessoaJuridica = rs.getInt(1);

            String sqlUpdatePessoa = String.format(
                    "UPDATE Pessoa SET nome = '%s', endereco = '%s', cidade = '%s', estado='%s', telefone = '%s',  email = '%s'  WHERE idPessoa = %s;",
                    novaPessoa.getNome(), novaPessoa.getEndereco(), novaPessoa.getCidade(), novaPessoa.getEstado(),
                    novaPessoa.getTelefone(),
                    novaPessoa.getEmail(), id_pessoaAssociadaA_PessoaJuridica
            );
            System.out.println(sqlUpdatePessoa);

            cnx.update(sqlUpdatePessoa);
        } else {
            System.out.println("Nenhum registro encontrado para o id = " + novaPessoa.getId());
        }
    }

    public void excluir(Pessoa p) throws SQLException {
        ResultSet rs = cnx.getSelect("SELECT idPessoa FROM PessoaJuridica pj WHERE pj.idPessoa = " + p.getId() + ";");
        if (rs.next()) {
            int id_pessoaAssociadaA_PessoaJuridica = rs.getInt(1);
            String sqlDeletePessoaJuridica = "DELETE FROM PessoaJuridica where idPessoa = " + p.getId() + ";";
            cnx.update(sqlDeletePessoaJuridica);
            String sqlDeletePessoa = "DELETE FROM Pessoa WHERE idPessoa = " + id_pessoaAssociadaA_PessoaJuridica + ";";
            cnx.update(sqlDeletePessoa);
        } else {
            System.out.println("Nenhum registro encontrado para excluir do id = " + p.getId());
        }
    }

    public void close() throws SQLException {
        cnx.close();
    }
}