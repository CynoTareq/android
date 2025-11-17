package it.cynomys.cfmandroid.silo

object SiloModels {

    val SILO_MODELS: List<SiloModelSpec> = listOf(

        SiloModelSpec("MO20", "Eurosilos", 3.0, 2.4, 3577, 2377, 1200, 1620, 860, 3),
        SiloModelSpec("MO35", "Eurosilos", 6.0, 4.8, 4542, 3342, 1200, 1940, 860, 3),
        SiloModelSpec("MO50", "Eurosilos", 8.5, 6.8, 4892, 3692, 1200, 2095, 860, 3),
        SiloModelSpec("MO60", "Eurosilos", 10.0, 8.0, 5182, 3982, 1200, 2200, 860, 3),
        SiloModelSpec("MO70", "Eurosilos", 12.0, 9.6, 5497, 4297, 1200, 2300, 860, 3),
        SiloModelSpec("MO90", "Eurosilos", 15.0, 12.0, 6032, 4832, 1200, 2450, 860, 3),
        SiloModelSpec("MO100", "Eurosilos", 17.5, 14.0, 6477, 5277, 1200, 2390, 860, 3),
        SiloModelSpec("MO120", "Eurosilos", 20.0, 16.0, 6777, 5577, 1200, 2500, 860, 4),
        SiloModelSpec("MO145", "Eurosilos", 24.0, 19.2, 7567, 6367, 1200, 2550, 860, 4),
        SiloModelSpec("MO170", "Eurosilos", 28.5, 22.8, 8222, 7022, 1200, 2600, 860, 4),
        SiloModelSpec("MO205", "Eurosilos", 34.0, 27.2, 9192, 7992, 1200, 2600, 860, 4),
        SiloModelSpec("MO300", "Eurosilos", 50.0, 40.0, 9911, 8911, 1000, 3000, 860, 4),
        SiloModelSpec("MO360", "Eurosilos", 60.0, 48.0, 11265, 10265, 1000, 3000, 860, 4),
        SiloModelSpec("MO370", "Eurosilos", 70.0, 56.0, 12690, 11690, 1000, 3000, 860, 4),
        SiloModelSpec("MO500", "Eurosilos", 84.0, 67.2, 14720, 13720, 1000, 3000, 860, 4),
        SiloModelSpec("MO565", "Eurosilos", 94.0, 75.2, 16095, 15095, 1000, 3000, 860, 4),

        SiloModelSpec("SIA TQS AN.06", "Agritech", 6.0, 3.6, 4010, 1050, 200, 2030, 2500, 4),
        SiloModelSpec("SIA TQS AN.08", "Agritech", 8.0, 4.8, 4800, 1050, 250, 2030, 2500, 4),
        SiloModelSpec("SIA TQS AN.10", "Agritech", 10.0, 6.0, 4930, 1050, 300, 2230, 2800, 4),
        SiloModelSpec("SIA TQS AN.12", "Agritech", 12.0, 7.2, 5570, 1050, 300, 2230, 2800, 4),
        SiloModelSpec("SIA TQS AN.15", "Agritech", 15.0, 9.0, 5680, 1050, 300, 2480, 3000, 4),
        SiloModelSpec("SIA TQS AN.18", "Agritech", 18.0, 10.8, 6450, 1050, 300, 2480, 3000, 4),
        SiloModelSpec("SIA TQS AN.20", "Agritech", 20.0, 12.0, 6990, 1050, 300, 2480, 3000, 4),
        SiloModelSpec("SIA TQS AN.22", "Agritech", 22.0, 13.2, 7480, 1050, 300, 2480, 3000, 4),
        SiloModelSpec("SIA TQS AN.25", "Agritech", 25.0, 15.0, 8330, 1050, 400, 2480, 3000, 4),
        SiloModelSpec("SIA TQS AN.31", "Agritech", 31.0, 18.6, 9220, 1050, 400, 2480, 3000, 4),
        SiloModelSpec("SIA TQS AN.40", "Agritech", 40.0, 24.0, 8850, 1050, 500, 2990, 3500, 4),
        SiloModelSpec("SIA TQS AN.52", "Agritech", 52.0, 31.2, 10780, 1050, 500, 2990, 3500, 4),

        SiloModelSpec("SM2", "CTS Calvinsilos", 2.0, 1.2, 2500, 200, 3200, 1150, 1100, 3),
        SiloModelSpec("SM3.5", "CTS Calvinsilos", 3.5, 2.1, 2500, 200, 3900, 1500, 1400, 3),
        SiloModelSpec("SM5", "CTS Calvinsilos", 5.0, 3.0, 2500, 200, 4100, 1700, 1500, 3),
        SiloModelSpec("SM6", "CTS Calvinsilos", 6.0, 3.5, 2500, 200, 4200, 1900, 1750, 3),
        SiloModelSpec("SM7.5", "CTS Calvinsilos", 7.5, 4.5, 2500, 200, 4750, 1900, 1750, 3),
        SiloModelSpec("SM8.5", "CTS Calvinsilos", 8.5, 5.1, 2500, 300, 5000, 1900, 1750, 3),
        SiloModelSpec("SM10", "CTS Calvinsilos", 10.0, 6.0, 2800, 300, 5400, 2000, 1850, 3),
        SiloModelSpec("SM12.5", "CTS Calvinsilos", 12.5, 7.5, 2800, 300, 5600, 2200, 1950, 3),
        SiloModelSpec("SM15", "CTS Calvinsilos", 15.0, 9.0, 3000, 300, 6200, 2300, 2050, 3),
        SiloModelSpec("SM17.5", "CTS Calvinsilos", 17.5, 10.5, 3000, 300, 6500, 2400, 1750, 4),
        SiloModelSpec("SM20", "CTS Calvinsilos", 20.0, 12.0, 3000, 300, 7100, 2450, 1800, 4),
        SiloModelSpec("SM25", "CTS Calvinsilos", 25.0, 15.0, 3000, 400, 7500, 2600, 1950, 4),
        SiloModelSpec("SM28", "CTS Calvinsilos", 28.0, 17.0, 3000, 400, 8100, 2600, 1950, 4),
        SiloModelSpec("SM31", "CTS Calvinsilos", 31.0, 19.0, 3000, 400, 8900, 2600, 1950, 4),
        SiloModelSpec("SM38", "CTS Calvinsilos", 38.0, 23.0, 4000, 500, 8250, 3000, 2250, 4),
        SiloModelSpec("SM52", "CTS Calvinsilos", 52.0, 31.2, 4000, 500, 9980, 3000, 2270, 4),
        SiloModelSpec("SM73", "CTS Calvinsilos", 73.0, 44.0, 4000, 500, 13000, 3000, 2300, 4)
    )
}