getFilteredData <- function(a_best_percent, a_data) {
	data_best = a_data[a_data$ap > quantile(a_data$ap, c(1.0 - a_best_percent), names=FALSE),]
	out = list()
	out$omega_min <- min(data_best$omega)
	out$omega_max <- max(data_best$omega)
	out$phi_min <- min(data_best$phi)
	out$phi_max <- max(data_best$phi)

	out$data <- a_data[a_data$phi >= out$phi_min & a_data$phi <= out$phi_max & a_data$omega >= out$omega_min & a_data$omega <= out$omega_max,]

	return(out)
}