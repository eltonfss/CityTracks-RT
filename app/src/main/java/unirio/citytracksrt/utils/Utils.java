package unirio.citytracksrt.utils;

import android.location.Location;

import java.util.Calendar;

/**
 * Class que armazena funções comuns a diversas classes
 */
public abstract class Utils {

    public static final int RAIO_DA_TERRA = 6378137;

    public static long calcularDiferencaEmSegundos(Calendar timestampMaisRecente, Calendar timestampMaisAntigo) {
        return (long) Math.floor((timestampMaisRecente.getTimeInMillis() - timestampMaisAntigo.getTimeInMillis()) / 1000);
    }

    /**
     * calcula a distância utilizando formula de Harvesine
     */
    public static double calcularDistanciaHarvesine(Location l1, Location l2) {

        Double distanciaDasLatitudes = Math.toRadians(l2.getLatitude() - l1.getLatitude());
        Double distanciaDasLongitudes = Math.toRadians(l2.getLongitude() - l1.getLongitude());

        Double a = Math.sin(distanciaDasLatitudes / 2) * Math.sin(distanciaDasLatitudes / 2)
                + Math.cos(Math.toRadians(l1.getLatitude())) * Math.cos(Math.toRadians(l2.getLatitude()))
                * Math.sin(distanciaDasLongitudes / 2) * Math.sin(distanciaDasLongitudes / 2);

        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distancia = RAIO_DA_TERRA * c; // converte para metros

        double altura = l1.getAltitude() - l2.getAltitude();

        distancia = Math.pow(distancia, 2) + Math.pow(altura, 2);

        return Math.sqrt(distancia);
    }

    public static float calcularDistancia(Location l1, Location l2) {
        float diferencaDeLatitudes = (float) (l2.getLatitude() - l1.getLatitude());
        float diferencaDeLongitudes = (float) (l2.getLongitude() - l1.getLongitude());

        return (float) Math.sqrt((Math.pow(converterGrausEmMetros(diferencaDeLatitudes), 2)) + ((Math.pow(converterGrausEmMetros(diferencaDeLongitudes), 2))));
    }

    public static double converterGrausEmMetros(double graus) {
        return graus * 60 * 1852;
    }

}
