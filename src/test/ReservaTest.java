package test;

import exception.ReservaException;
import service.ReservaService;

public class ReservaTest implements Test {

    private ReservaService reservaService;

    //Conectanto o test com service
    public ReservaTest(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    //Função test do listar
    public String listar() throws ReservaException {
        return reservaService.listar();
    }

    //Função de teste para cadastro
    public String cadastrar() throws ReservaException {
        return reservaService.cadastrar(
                "15/07/2198",
                "15/12/2200",
                null,
                1L,
                6,
                5000.00
        );
    }

    //Função de teste para alteração
    public String alterar() throws ReservaException {
        return reservaService.alterar(
                null,
                "13/12/2005",
                "15/12/2006",
                2L,
                2L,
                12,
                10000.00
        );
    }

    //Função de teste para exclusão
    public String excluir() throws ReservaException {
        return reservaService.excluir(13L);
    }
}
