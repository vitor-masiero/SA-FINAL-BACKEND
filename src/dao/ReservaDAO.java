package dao;

import connection.ConexaoMySQL;
import exception.ReservaException;
import model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class ReservaDAO implements DAO<Reserva> {

    //Selecionando a reserva completa do Banco
    public ArrayList<Reserva> selecionar() throws ReservaException {
        try {
            //Comando sql
            String sql = "SELECT " +
                    "r.id AS r_id, " +
                    "r.data_inicio AS r_data_inicio, " +
                    "r.data_fim AS r_data_fim, " +
                    "r.valor_total AS r_valor_total, " +
                    "r.qtd_hospedes AS r_qtd_hospedes, " +

                    "r.id_acomodacao AS r_id_acomodacao, " +
                    "a.id AS a_id, " +
                    "a.nome AS a_nome, " +
                    "a.valor_diaria AS a_valor_diaria, " +
                    "a.limite_hospedes AS a_limite_hospedes, " +
                    "a.descricao AS a_descricao, " +
                    "a.id_funcionario_responsavel AS a_id_funcionario_responsavel, " +

                    "f.id AS f_id, " +
                    "f.id_pessoa AS f_id_pessoa, " +
                    "pf.nome_completo AS pf_nome_completo, " +
                    "pf.data_nascimento AS pf_data_nascimento, " +
                    "pf.documento AS pf_documento, " +
                    "pf.pais AS pf_pais, " +
                    "pf.estado AS pf_estado, " +
                    "pf.cidade AS pf_cidade, " +
                    "f.cargo AS f_cargo, " +
                    "f.salario AS f_salario, " +

                    "r.id_cliente_responsavel AS r_id_cliente_responsavel, " +
                    "c.id AS c_id, " +
                    "c.id_pessoa AS c_id_pessoa, " +
                    "pc.nome_completo AS pc_nome_completo, " +
                    "pc.data_nascimento AS pc_data_nascimento, " +
                    "pc.documento AS pc_documento, " +
                    "pc.pais AS pc_pais, " +
                    "pc.estado AS pc_estado, " +
                    "pc.cidade AS pc_cidade, " +
                    "c.fidelidade AS c_fidelidade, " +
                    "c.observacao AS c_observacao " +

                    "FROM reserva r " +
                    "JOIN acomodacao a ON a.id = r.id_acomodacao " +
                    "JOIN cliente c ON c.id = r.id_cliente_responsavel " +
                    "JOIN funcionario f ON f.id = a.id_funcionario_responsavel " +
                    "JOIN pessoa pc ON pc.id = c.id_pessoa " +
                    "JOIN pessoa pf ON pf.id = f.id_pessoa;";
            Statement declaracao = ConexaoMySQL.get().createStatement();
            ResultSet resultado = declaracao.executeQuery(sql);


            ArrayList<Reserva> reservas = new ArrayList<>();

            //Criando a reserva
            while (resultado.next()) {
                Funcionario funcionario = new Funcionario(
                        resultado.getLong("f_id"),
                        resultado.getString("pf_nome_completo"),
                        resultado.getDate("pf_data_nascimento").toLocalDate(),
                        resultado.getString("pf_documento"),
                        resultado.getString("pf_pais"),
                        resultado.getString("pf_estado"),
                        resultado.getString("pf_cidade"),
                        resultado.getLong("f_id_pessoa"), // id pessoa do funcionário
                        resultado.getString("f_cargo"),
                        resultado.getDouble("f_salario")
                );

                //Criando o cliente
                Cliente cliente = new Cliente(
                        resultado.getLong("c_id_pessoa"),
                        resultado.getString("pc_nome_completo"),
                        resultado.getDate("pc_data_nascimento").toLocalDate(),
                        resultado.getString("pc_documento"),
                        resultado.getString("pc_pais"),
                        resultado.getString("pc_estado"),
                        resultado.getString("pc_cidade"),
                        resultado.getLong("c_id_pessoa"), // id pessoa do cliente
                        resultado.getBoolean("c_fidelidade"),
                        resultado.getString("c_observacao")
                );

                //Criando a acomodacao
                Acomodacao acomodacao = new Acomodacao(
                        resultado.getLong("a_id"),
                        resultado.getString("a_nome"),
                        resultado.getDouble("a_valor_diaria"),
                        resultado.getInt("a_limite_hospedes"),
                        resultado.getString("a_descricao"),
                        funcionario // objeto Funcionario
                );

                //Criando a reserva completa, depois de criar todas as classes que estão inseridas nela
                Reserva reserva = new Reserva(
                        resultado.getLong("r_id"),
                        resultado.getDate("r_data_inicio").toLocalDate(),
                        resultado.getDate("r_data_fim").toLocalDate(),
                        acomodacao, // objeto Acomodacao
                        cliente, // objeto Cliente
                        resultado.getInt("r_qtd_hospedes"),
                        resultado.getDouble("r_valor_total")
                );
                reservas.add(reserva);
            }
            return reservas;

            //Exception da seleção
        } catch (SQLException e) {
            throw new ReservaException("Erro ao selecionar a reserva! Por favor, tente novamente mais tarde.");
        }
    }

    //Inserindo uma reserva no banco de dados
    public Boolean inserir(Reserva reserva) throws ReservaException {
        try {
            String sql = "INSERT INTO reserva " +
                    "(data_inicio, data_fim, valor_total, qtd_hospedes, id_acomodacao, id_cliente_responsavel) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement declaracao = ConexaoMySQL.get().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Definindo os parâmetros
            declaracao.setDate(1, java.sql.Date.valueOf(reserva.getDataInicio()));
            declaracao.setDate(2, java.sql.Date.valueOf(reserva.getDataFim()));
            declaracao.setDouble(3, reserva.getValorTotal());
            declaracao.setInt(4, reserva.getQtdHospedes());
            declaracao.setLong(5, reserva.getAcomodacao().getId()); // ID da acomodação
            declaracao.setLong(6, reserva.getClienteResponsavel().getId()); // ID do cliente

            // Executando a inserção
            int linhasAfetadas = declaracao.executeUpdate();

            // Verificando se a inserção foi bem-sucedida
            if (linhasAfetadas == 0) {
                throw new ReservaException("Erro ao inserir a reserva no banco. Nenhuma linha foi inserida.");
            }

            // Recuperando o ID gerado automaticamente da reserva inserida
            try (ResultSet generatedKeys = declaracao.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reserva.setId(generatedKeys.getLong(1));
                } else {
                    throw new ReservaException("Falha ao obter o ID da reserva inserida.");
                }
            }
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ReservaException("Erro ao inserir a reserva! Por favor, tente novamente mais tarde.");
        }
    }

    //Atualizando a reserva no banco de dados
    public Boolean atualizar(Reserva reserva) throws ReservaException {
        try {
            String sql = "UPDATE reserva " +
                    "SET " +
                    "id_acomodacao = ?, " +
                    "id_cliente_responsavel = ?, " +
                    "data_inicio = ?, " +
                    "data_fim = ?, " +
                    "valor_total = ?, " +
                    "qtd_hospedes = ? " +
                    "WHERE id = ?";

            //Preparando e passando os parâmetros
            PreparedStatement declaracao = ConexaoMySQL.get().prepareStatement(sql);
            declaracao.setLong(1, reserva.getAcomodacao().getId());
            declaracao.setLong(2, reserva.getClienteResponsavel().getId());
            declaracao.setDate(3, Date.valueOf(reserva.getDataInicio()));
            declaracao.setDate(4, Date.valueOf(reserva.getDataFim()));
            declaracao.setDouble(5, reserva.getValorTotal());
            declaracao.setInt(6, reserva.getQtdHospedes());
            declaracao.setLong(7, reserva.getId());

            //Executando o update
            return declaracao.executeUpdate() > 0;

        } catch(Exception e) {
            throw new ReservaException("Erro ao atualizar a reserva. Tente novamente mais tarde!");
        }
    }

    //Deletando diretamente no banco de dados
    public Boolean deletar(Long id) throws ReservaException {
        try {
            //Comando sql com DELETE
            String sql = "DELETE FROM reserva WHERE id = ?";

            //Passando o id para o WHERE
            PreparedStatement preparacao = ConexaoMySQL.get().prepareStatement(sql);
            preparacao.setLong(1, id);
            return preparacao.executeUpdate() > 0;

        } catch (Exception e) {
            throw new ReservaException("Erro ao deletar a reserva. Por favor, tente novamente mais tarde.");
        }
    }

    //Selecionar uma reserva diretamente pelo ID
    public Reserva selecionarPorId(Long id) throws ReservaException {
        try {
            String sql = "SELECT " +
                    "id, " +
                    "id_acomodacao, " +
                    "id_cliente_responsavel, " +
                    "data_inicio, " +
                    "data_fim, " +
                    "valor_total, " +
                    "qtd_hospedes " +
                    "FROM reserva " +
                    "WHERE id = ?;";

            PreparedStatement preparacao = ConexaoMySQL.get().prepareStatement(sql);
            preparacao.setLong(1, id);
            ResultSet resultado = preparacao.executeQuery();

            //Selecionando todos os atributos e criando uma reserva
            if (resultado.next()) {
                return new Reserva(
                    resultado.getLong("id"),
                    resultado.getDate("data_inicio").toLocalDate(),
                    resultado.getDate("data_fim").toLocalDate(),
                    selecionarAcomodacaoPorId(resultado.getLong("id_cliente_responsavel")),
                    selecionarClientePorId(resultado.getLong("id_acomodacao")),
                    resultado.getInt("qtd_hospedes"),
                    resultado.getDouble("valor_total")
                );
            } else {
                    return null;
            }

        } catch (Exception e) {
            throw new ReservaException("Não foi possível selecionar a reserva.");
        }
    }
    //Selecionando um cliente específico pelo seu id
    public Cliente selecionarClientePorId(Long id) throws ReservaException {
        try {
            String sql = "SELECT " +
                    "id, " +
                    "id_pessoa, " +
                    "fidelidade, " +
                    "observacao " +
                    "FROM cliente " +
                    "WHERE id = ?";

            PreparedStatement preparacao = ConexaoMySQL.get().prepareStatement(sql);
            preparacao.setLong(1, id);
            ResultSet resultado = preparacao.executeQuery();

            //Criando o cliente do id com os dados do banco
            if (resultado.next()) {
                return new Cliente(
                    resultado.getLong("id"),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    resultado.getLong("id_pessoa"),
                    resultado.getBoolean("fidelidade"),
                    resultado.getString("observacao")
                );
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new ReservaException("Não foi possível selecionar o cliente pelo ID.");
        }
    }

    //Selecionando uma acomodação específica pelo seu ID
    public Acomodacao selecionarAcomodacaoPorId(Long id) throws ReservaException {
        try {
            String sql = "SELECT " +
                    "id, " +
                    "nome," +
                    "valor_diaria, " +
                    "limite_hospedes, " +
                    "descricao, " +
                    "id_funcionario_responsavel " +
                    "FROM acomodacao " +
                    "WHERE id = ?";

            PreparedStatement preparacao = ConexaoMySQL.get().prepareStatement(sql);
            preparacao.setLong(1, id);
            ResultSet resultado = preparacao.executeQuery();

            //Criando a acomodação com o id de teste
            if (resultado.next()) {
                return new Acomodacao(
                    resultado.getLong("id"),
                    resultado.getString("nome"),
                    resultado.getDouble("valor_diaria"),
                    resultado.getInt("limite_hospedes"),
                    resultado.getString("descricao"),
                    null
                );
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new ReservaException("Não foi possível selecionar a acomodação por ID.");
        }
    }

    //Verificando se ainda há disponibilidade de uma acomodação em um certo período
    public Boolean verificarReservaPorAcomodacaoEPeriodo(Long idAcomodacao, LocalDate dataInicio, LocalDate dataFim) throws ReservaException {
        try {
            String sql = "SELECT id " +
                         "FROM reserva " +
                         "WHERE " +
                         "id_acomodacao = ? " +
                         "AND (? BETWEEN data_inicio AND data_fim OR ? BETWEEN data_inicio AND data_fim)";

            PreparedStatement preparacao = ConexaoMySQL.get().prepareStatement(sql);
            preparacao.setLong(1, idAcomodacao);
            preparacao.setDate(2, Date.valueOf(dataInicio));
            preparacao.setDate(3, Date.valueOf(dataFim));
            ResultSet resultado = preparacao.executeQuery();
            return resultado.next();

        } catch (Exception e) {
            throw new ReservaException("Erro ao verificar disponibilidade da acomodação. Tente novamente mais tarde.");
        }
    }

}