rm(list=ls(all=TRUE))
setwd("C:/hObbE/projects/coding/research/s4rdm3x/r-scripts")

source("functions/getFilteredData.R")
source("functions/loadData.R")

plotAPMR <- function(a_data) {
	oldpar <- par(mfrow = c(2,2), oma = c(5,2,2,0) + 0.1, mar = c(4,4,2,1) + 0.1)
	plot(ap~omega, data=a_data, ylab="auto perf", xlab="")
	plot(ap~phi, data=a_data, ylab="", xlab="")
	plot(mr~omega, data=a_data, xlab="omega", ylab="mapping rate")
	plot(mr~phi, data=a_data, xlab="phi", ylab="")
	par(oldpar)
}

data <- loadData("jabref_fanin.csv")
plotAPMR(data)
final = getFilteredData(0.05, data)
plotAPMR(final$data)
	
