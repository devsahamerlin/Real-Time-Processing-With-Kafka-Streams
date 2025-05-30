package com.merlin.app;

import com.merlin.app.config.KafkaConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;

public class WeatherDataProcessor {

    public static void main(String[] args) {
        KafkaConfig kafkaConfig = new KafkaConfig();

        StreamsBuilder builder = new StreamsBuilder();

        // Lire les données météorologiques depuis le topic 'weather-data'
        KStream<String, String> weatherStream = builder.stream("weather-data");

        // Filtrer les températures élevées (> 30°C)
        // Le format des données est: station,temperature,humidity
        KStream<String, String> highTempStream = weatherStream.filter((key, value) -> {
            try {
                String[] parts = value.split(",");
                if (parts.length >= 3) {
                    double temperature = Double.parseDouble(parts[1]);
                    return temperature > 30.0; // Ne garder que les températures > 30°C
                }
                return false;
            } catch (Exception e) {
                // En cas d'erreur de parsing, ignorer ce message
                System.err.println("Erreur lors du parsing: " + value);
                return false;
            }
        });

        // Convertir les températures en Fahrenheit
        // Formule: °F = (°C × 9/5) + 32
        KStream<String, String> fahrenheitStream = highTempStream.mapValues(value -> {
            try {
                String[] parts = value.split(",");
                String station = parts[0];
                double celsius = Double.parseDouble(parts[1]);
                String humidity = parts[2];

                // Conversion Celsius vers Fahrenheit
                double fahrenheit = (celsius * 9.0 / 5.0) + 32.0;

                // Reconstituer le message avec la température en Fahrenheit
                return station + "," + String.format("%.1f", fahrenheit) + "," + humidity;
            } catch (Exception e) {
                System.err.println("Erreur lors de la conversion: " + value);
                return value; // Retourner la valeur originale en cas d'erreur
            }
        });

        // Grouper par station et calculer les moyennes
        // D'abord, nous devons extraire la station comme clé pour le groupement
        KStream<String, String> keyedByStation = fahrenheitStream.selectKey((key, value) -> {
            String[] parts = value.split(",");
            return parts[0]; // La station devient la clé
        });

        // Grouper par station
        KGroupedStream<String, String> groupedByStation = keyedByStation.groupByKey();

        // Calculer les moyennes de température et d'humidité
        // Nous utilisons une structure pour accumuler les valeurs
        KTable<String, String> stationAverages = groupedByStation.aggregate(
                () -> "0,0,0", // Valeur initiale: sommeTemp,sommeHumidite,count
                (station, newValue, aggregate) -> {
                    try {
                        String[] newParts = newValue.split(",");
                        double newTemp = Double.parseDouble(newParts[1]);
                        double newHumidity = Double.parseDouble(newParts[2]);

                        String[] aggParts = aggregate.split(",");
                        double sumTemp = Double.parseDouble(aggParts[0]);
                        double sumHumidity = Double.parseDouble(aggParts[1]);
                        int count = Integer.parseInt(aggParts[2]);

                        // Mettre à jour les sommes et le compteur
                        sumTemp += newTemp;
                        sumHumidity += newHumidity;
                        count++;

                        return sumTemp + "," + sumHumidity + "," + count;
                    } catch (Exception e) {
                        System.err.println("Erreur lors de l'agrégation: " + newValue);
                        return aggregate; // Retourner l'agrégat actuel en cas d'erreur
                    }
                },
                Materialized.with(Serdes.String(), Serdes.String())
        );

        // Transformer les agrégats en moyennes finales
        KStream<String, String> averageStream = stationAverages.toStream().mapValues((station, aggregate) -> {
            try {
                String[] parts = aggregate.split(",");
                double sumTemp = Double.parseDouble(parts[0]);
                double sumHumidity = Double.parseDouble(parts[1]);
                int count = Integer.parseInt(parts[2]);

                if (count > 0) {
                    double avgTemp = sumTemp / count;
                    double avgHumidity = sumHumidity / count;

                    return String.format("%s : Température Moyenne = %.1f°F, Humidité Moyenne = %.1f%%",
                            station, avgTemp, avgHumidity);
                } else {
                    return station + " : Aucune donnée";
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du calcul des moyennes: " + aggregate);
                return station + " : Erreur de calcul";
            }
        });

        // Publier les résultats dans le topic 'station-averages'
        averageStream.to("station-averages");

        // Optionnel: Afficher les résultats dans la console pour le débogage
        averageStream.foreach((station, average) -> {
            System.out.println("Résultat pour " + station + ": " + average);
        });

        // Démarrer l'application Kafka Streams
        KafkaStreams streams = new KafkaStreams(builder.build(), kafkaConfig.getKafkaProperties());

        // Ajouter un hook pour un arrêt propre de l'application
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Arrêt de l'application Kafka Streams...");
            streams.close();
        }));

        try {
            streams.start();
            System.out.println("Application Kafka Streams démarrée avec succès!");
            System.out.println("En attente de données météorologiques...");
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}