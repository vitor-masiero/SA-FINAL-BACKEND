package controller;

import enumeration.Funcionalidade;
import exception.ReservaException;
import model.Reserva;
import test.ReservaTest;

public class ReservaController {
    private ReservaTest reservaTest;

    //Conectanto test com controller
    public ReservaController(ReservaTest reservaTest) {
        this.reservaTest = reservaTest;
    }

    //Função de conexao com o RESERVATEST
    public String testar(Funcionalidade funcionalidade) throws ReservaException {
        switch (funcionalidade) {
            case LISTAR:
                return reservaTest.listar();
            case CADASTRAR:
                return reservaTest.cadastrar();
            case ALTERAR:
                return reservaTest.alterar();
            case EXCLUIR:
                return reservaTest.excluir();
            default:
                return null;
        }
    }
}
