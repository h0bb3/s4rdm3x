loadData <- function(a_csvFile) {
	data <- read.csv(a_csvFile, head=TRUE, sep="\t")
	data$mr = (data$totalManuallyClustered + data$totalAutoClustered) / (data$totalMapped - data$initialClustered)
	data$ap = (data$totalAutoClustered - data$totalAutoWrong) / data$totalMapped
	return(data)
}