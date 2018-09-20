rm(list=ls(all=TRUE))
setwd("C:/hObbE/projects/coding/research/s4rdm3x/r-scripts")

source("functions/getFilteredData.R")
source("functions/loadData.R")

plotAPMRMP <- function(a_data) {
	oldpar <- par(mfrow = c(3,2), oma = c(0,0,0,0) + 0.1, mar = c(5,5,1,1) + 0.1)
	plot(ap~omega, data=a_data, ylab="auto perf", xlab="omega")
	plot(ap~phi, data=a_data, ylab="", xlab="phi")
	plot(mr~omega, data=a_data, xlab="omega", ylab="mapping rate")
	plot(mr~phi, data=a_data, xlab="phi", ylab="")
	plot(mp~omega, data=a_data, xlab="omega", ylab="man perf")
	plot(mp~phi, data=a_data, xlab="phi", ylab="")
	par(oldpar)
}

data <- loadData("lucene/linecount.csv")
data = data[data$mappingPercent <= 0.2,]
plotAPMRMP(data)
final = getFilteredData(0.1, 25, data)
plotAPMRMP(final$data)
	
