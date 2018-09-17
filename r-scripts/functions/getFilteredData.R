getFilteredData <- function(a_best_percent, a_maxInitialCluster, a_data) {
	data_filtered = a_data[a_data$initialClustered >= a_maxInitialCluster & a_data$initialClustered >= 10,]
	data_best = data_filtered[data_filtered$ap > quantile(data_filtered$ap, c(1.0 - a_best_percent), names=FALSE),]
	out = list()
	out$omega_min <- min(data_best$omega)
	out$omega_max <- max(data_best$omega)
	out$phi_min <- min(data_best$phi)
	out$phi_max <- max(data_best$phi)

	out$data <- a_data[a_data$initialClustered <= a_maxInitialCluster & a_data$initialClustered >= 10 & a_data$phi >= out$phi_min & a_data$phi <= out$phi_max & a_data$omega >= out$omega_min & a_data$omega <= out$omega_max,]

	return(out)
}