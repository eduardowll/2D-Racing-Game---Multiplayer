package Client;

///////////////////////////////////////////
//
//  Sid: 1955004
//
///////////////////////////////////////////
/**
 Set ipAddress with "localhost" or preferred ipAddresses
 Set nLaps to desired number of laps
 **/

import javax.swing.*;

///////////////////////////////////////////
public class Main {

    static String ipAddress = "26.210.145.27";
    static int nLaps = 1;

    public static void main(String[] args) {
        // Cria as opções do botão
        Object[] opcoes = {"UDP", "TCP"};

        // Abre a janelinha
        int escolha = JOptionPane.showOptionDialog(null,
                "Qual protocolo você quer usar para jogar?",
                "Modo de Conexão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoes,
                opcoes[0]);

        // Se o cara fechar a janela no X, o programa fecha
        if (escolha == JOptionPane.CLOSED_OPTION) {
            System.exit(0);
        }

        // Se clicou no botão 1 (TCP), a variável vira 'true'
        boolean usarTCP = (escolha == 1);

        // Passamos o 'usarTCP' para o Frame!
        Frame frame = new Frame(ipAddress, nLaps, usarTCP);
        frame.setVisible(true);
    }
}