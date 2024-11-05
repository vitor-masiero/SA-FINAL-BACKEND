
package service;

import dao.ReservaDAO;
import enumeration.Funcionalidade;
import exception.ReservaException;
import model.Acomodacao;
import model.Cliente;
import model.Reserva;
import util.Util;

import java.time.LocalDate;
import java.util.ArrayList;

//Classe de serviço para gerenciar reservas: listar, cadastrar, alterar e excluir.

public class ReservaService {

    private ReservaDAO reservaDAO;

    //Construtor que inicializa o serviço com uma instância de ReservaDAO.

    public ReservaService(ReservaDAO reservaDAO) {
        this.reservaDAO = reservaDAO;
    }

    //Lista todas as reservas.

    //@return String com reservas ou mensagem de ausência.

    public String listar() throws ReservaException {
        ArrayList<Reserva> reservas = reservaDAO.selecionar();
        StringBuilder lista = new StringBuilder();
        if (reservas.size() > 0) {
            for (Reserva reserva : reservas) {
                lista.append(reserva).append("\n");
            }
        } else {
            lista.append("Nenhuma reserva encontrada.");
        }
        return lista.toString();
    }

    //Cadastra uma nova reserva após validar os dados.
    public String cadastrar(String dataInicioFormatoBR, String dataFimFormatoBR, Long idAcomodacao, Long idCliente, Integer qtdHospedes, Double valorTotal) throws ReservaException {
        String mensagemErro = validarCampos(Funcionalidade.CADASTRAR, null, dataInicioFormatoBR, dataFimFormatoBR, idAcomodacao, idCliente, qtdHospedes);
        if (!mensagemErro.isEmpty()) throw new ReservaException(mensagemErro);

        LocalDate dataInicio = Util.formatarStringParaLocalDate(dataInicioFormatoBR);
        LocalDate dataFim = Util.formatarStringParaLocalDate(dataFimFormatoBR);
        Acomodacao acomodacao = reservaDAO.selecionarAcomodacaoPorId(idAcomodacao);
        Cliente cliente = reservaDAO.selecionarClientePorId(idCliente);

        Reserva reserva = new Reserva(dataInicio, dataFim, acomodacao, cliente, qtdHospedes, valorTotal);
        if (reservaDAO.inserir(reserva)) {
            return "Reserva cadastrada com sucesso!";
        } else {
            throw new ReservaException("Erro ao cadastrar a reserva.");
        }
    }


    //Altera uma reserva existente.

    public String alterar(Long id, String dataInicioFormatoBR, String dataFimFormatoBR, Long idAcomodacao, Long idCliente, Integer qtdHospedes, Double valorTotal) throws ReservaException {
        String mensagemErro = validarCampos(Funcionalidade.ALTERAR, id, dataInicioFormatoBR, dataFimFormatoBR, idAcomodacao, idCliente, qtdHospedes);
        if (!mensagemErro.isEmpty()) throw new ReservaException(mensagemErro);

        LocalDate dataInicio = Util.formatarStringParaLocalDate(dataInicioFormatoBR);
        LocalDate dataFim = Util.formatarStringParaLocalDate(dataFimFormatoBR);
        Reserva reserva = new Reserva(id, dataInicio, dataFim, reservaDAO.selecionarAcomodacaoPorId(idAcomodacao), reservaDAO.selecionarClientePorId(idCliente), qtdHospedes, valorTotal);

        if (reservaDAO.atualizar(reserva)) {
            return "Reserva alterada com sucesso!";
        } else {
            throw new ReservaException("Erro ao alterar a reserva.");
        }
    }

    //Exclui uma reserva pelo ID.

    public String excluir(Long id) throws ReservaException {
        String mensagemErro = validarCampos(Funcionalidade.EXCLUIR, id, null, null, null, null, null);
        if (!mensagemErro.isEmpty()) throw new ReservaException(mensagemErro);

        if (reservaDAO.deletar(id)) {
            return "Reserva excluída com sucesso!";
        } else {
            throw new ReservaException("Erro ao excluir a reserva.");
        }
    }


    //Valida campos de acordo com a operação (cadastrar, alterar ou excluir).

    private String validarCampos(Funcionalidade funcionalidade, Long id, String dataInicioFormatoBR, String dataFimFormatoBR, Long idAcomodacao, Long idCliente, Integer qtdHospedes) throws ReservaException {
        StringBuilder erros = new StringBuilder();

        // Validação de ID para alteração ou exclusão
        if ((funcionalidade == Funcionalidade.ALTERAR || funcionalidade == Funcionalidade.EXCLUIR) && (id == null || reservaDAO.selecionarPorId(id) == null)) {
            erros.append("\n- Id inválido ou não encontrado.");
        }

        // Validação de campos obrigatórios para cadastro e alteração
        if (funcionalidade == Funcionalidade.CADASTRAR || funcionalidade == Funcionalidade.ALTERAR) {

            if (qtdHospedes == null || qtdHospedes == 0) {
                erros.append("\n- Quantidade de hóspedes é obrigatória.");
            }

            if (dataInicioFormatoBR == null || !Util.validarDataFormatoBR(dataInicioFormatoBR)) {
                erros.append("\n- Data de início inválida.");
            }

            if (dataFimFormatoBR == null || !Util.validarDataFormatoBR(dataFimFormatoBR)) {
                erros.append("\n- Data de fim inválida.");
            }

            if (idAcomodacao == null || reservaDAO.selecionarAcomodacaoPorId(idAcomodacao) == null) {
                erros.append("\n- ID da acomodação inválido.");
            }

            if (idCliente == null || reservaDAO.selecionarClientePorId(idCliente) == null) {
                erros.append("\n- ID do cliente inválido.");
            }

            // Verificação de disponibilidade de datas
            // Valida se uma data está disponível para certa acomodação.
            if(idAcomodacao != null && dataInicioFormatoBR != null && dataFimFormatoBR != null) {
                Boolean indisponivel = reservaDAO.verificarReservaPorAcomodacaoEPeriodo(idAcomodacao,
                        Util.formatarStringParaLocalDate(dataInicioFormatoBR),
                        Util.formatarStringParaLocalDate(dataFimFormatoBR));
                if(indisponivel) {
                    erros.append("\n- Data indisponível para acomodação.");
                }
            }
        }
        return erros.length() > 0 ? "Erro(s) na validação: " + erros.toString() : "";
    }
}