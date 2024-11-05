import controller.ReservaController;
import dao.ReservaDAO;
import enumeration.Funcionalidade;
import exception.ReservaException;
import service.ReservaService;
import test.ReservaTest;

public class Principal {

    public static void main(String[] args) throws ReservaException {
        // Inicialização de objetos
        ReservaDAO reservaDAO = new ReservaDAO();
        ReservaService reservaService = new ReservaService(reservaDAO);
        ReservaTest reservaTest = new ReservaTest(reservaService);
        ReservaController reservaController = new ReservaController(reservaTest);

        // Informações do teste
        Funcionalidade funcionalidade = Funcionalidade.CADASTRAR;
        System.out.println("RESERVA: " + funcionalidade);
        System.out.println("RESULTADO DO TESTE:");

        // Realização do teste
        try {
            System.out.println(reservaController.testar(funcionalidade));
        } catch (ReservaException excecao) {
            System.err.println(excecao.getMessage());
        }
    }
   

}
