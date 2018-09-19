loadData <- function(a_csvFile) {
	data <- read.csv(a_csvFile, head=TRUE, sep="\t")
	data$mr = (data$totalManuallyClustered + data$totalAutoClustered) / (data$totalMapped - data$initialClustered)
	data$ap = (data$totalAutoClustered - data$totalAutoWrong) / (data$totalMapped - data$initialClustered)
	data$mp = ifelse(data$totalManuallyClustered > 0, (data$totalManuallyClustered - data$totalFailedClusterings) / data$totalManuallyClustered, data$mr)
	
	# harmonic means for the performance metrics
	data$h_mam = ifelse(data$ap == 0 | data$mp == 0 | data$mr == 0, 0, 3*data$ap*data$mr*data$mp/(data$ap*data$mr + data$ap*data$mp + data$mp*data$mr))
	data$h_am = ifelse(data$ap == 0 | data$mp == 0, 0, 2*data$ap*data$mp/(data$ap+data$mp))
	return(data)
}