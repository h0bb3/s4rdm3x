getFilteredData <- function(a_best_percent, a_maxInitialCluster, a_data) {
	data_filtered = a_data[a_data$mappingPercent <= 0.2,]
	data_best = data_filtered[data_filtered$h_mam > quantile(data_filtered$h_mam, c(1.0 - a_best_percent), names=FALSE),]
	omegaphi = getOmegaPhi(a_best_percent, a_data)
	out = list()
	out$omega_min <- omegaphi$omega_min
	out$omega_max <- omegaphi$omega_max
	out$phi_min <- omegaphi$phi_min
	out$phi_max <- omegaphi$phi_max

	out$data <- a_data[a_data$initialClustered <= a_maxInitialCluster & a_data$initialClustered >= 10 & a_data$phi >= out$phi_min & a_data$phi <= out$phi_max & a_data$omega >= out$omega_min & a_data$omega <= out$omega_max,]

	return(out)
}

getOmegaPhi <- function(a_best_percent, a_data) {
	#data_filtered = a_data[a_data$mappingPercent <= 0.2 & a_data$mappingPercent >= 0.175,]
	data_filtered = a_data[a_data$mappingPercent <= 0.2,]
	data_best = data_filtered[data_filtered$h_mam > quantile(data_filtered$h_mam, c(1.0 - a_best_percent), names=FALSE),]
	out = list()
	out$omega_min <- min(data_best$omega)
	out$omega_max <- max(data_best$omega)
	out$phi_min <- min(data_best$phi)
	out$phi_max <- max(data_best$phi)
	return(out)
}

getOmegaPhiInRange <- function(a_best_percent, a_data, a_min, a_max) {
	#data_filtered = a_data[a_data$mappingPercent <= 0.2 & a_data$mappingPercent >= 0.175,]
	data_filtered = a_data[a_data$initialClustered <= a_max & a_data$initialClustered > a_min,]
	data_best = data_filtered[data_filtered$h_mam > quantile(data_filtered$h_mam, c(1.0 - a_best_percent), names=FALSE),]
	out = list()
	out$omega_min <- min(data_best$omega)
	out$omega_max <- max(data_best$omega)
	out$phi_min <- min(data_best$phi)
	out$phi_max <- max(data_best$phi)
	return(out)
}


getFilteredData_OmegaPhi <- function(a_minInitialCluster, a_maxInitialCluster, a_omegaphi, a_data) {
	
	out = list()
	out$omega_min <- a_omegaphi$omega_min
	#out$omega_min <- 0
	out$omega_max <- a_omegaphi$omega_max
	out$phi_min <- a_omegaphi$phi_min
	out$phi_max <- a_omegaphi$phi_max
	out$data <- a_data[a_data$initialClustered <= a_maxInitialCluster & a_data$initialClustered >= a_minInitialCluster & a_data$phi >= out$phi_min & a_data$phi <= out$phi_max & a_data$omega >= out$omega_min & a_data$omega <= out$omega_max,]
	
	return(out)
}

getFilteredData_P_OmegaPhi <- function(a_minInitialCluster_p, a_maxInitialCluster_p, a_omegaphi, a_data) {
	
	out = list()
	out$omega_min <- a_omegaphi$omega_min
	out$omega_max <- a_omegaphi$omega_max
	out$phi_min <- a_omegaphi$phi_min
	out$phi_max <- a_omegaphi$phi_max
	out$data <- a_data[a_data$initialClusteredPercent <= a_maxInitialCluster_p & a_data$initialClusteredPercent >= a_minInitialCluster_p & a_data$phi >= out$phi_min & a_data$phi <= out$phi_max & a_data$omega >= out$omega_min & a_data$omega <= out$omega_max,]
	
	return(out)
}



