# curated script for ICSA2019 submission and replication
rm(list=ls(all=TRUE))
library(coin)
setwd("C:/hObbE/projects/coding/research/s4rdm3x/r-scripts/ICSA2019")

source("functions/getFilteredData.R")
source("functions/loadData.R")
source("functions/util.R")

# load all datafiles
jabref_loaded <- loadData("data/jabref_all.csv")
teammates_loaded <- loadData("data/teammates_all.csv")
argouml_loaded <- loadData("data/argouml_all.csv")
ant_loaded <- loadData("data/ant_all.csv")
lucene_loaded <- loadData("data/lucene_all.csv")

nrow(jabref_loaded)
nrow(teammates_loaded)
nrow(argouml_loaded)
nrow(ant_loaded)
nrow(lucene_loaded)

nrow(jabref_loaded)+nrow(teammates_loaded)+nrow(argouml_loaded)+nrow(ant_loaded)+nrow(lucene_loaded)

# function that computes the interval data, e.g. finds the best metric in each 5 elements interval from 0-50 elements
checkEveryInterval <- function(a_data) {

	best_percent = 0.01
	rows <- NULL
	for (i in 0:9) {
		min_multiplier <- (0.0 + i * 0.01) * 1
		max_multiplier <- (0.0 + (i + 1) * 0.01) * 1

		#min <- min_multiplier * a_data$totalMapped[1]
		#max <- a_data$totalMapped[1]*max_multiplier

		min <- i * 5
		max <- (i + 1) * 5

		min_multiplier <- min / a_data$totalMapped[1]
		max_multiplier <- max / a_data$totalMapped[1]

		if (max < 1) {
			max <- 1
		}

		filtered_data <- invisible(analyzeSystem(best_percent, a_data, min, max))

		#message(summary(filtered_data$data))
		ordered_metrics <- with(filtered_data$data, reorder(metric, -h_mam, FUN=median))
		#boxplot(h_mam~ordered_metrics, data=filtered_data$data, las=2)
		#title(a_data$system[1])

		topMetric <- levels(ordered_metrics)[1]
		message(topMetric)

		selected_metric <- filtered_data$data$h_mam[filtered_data$data$metric==topMetric]
		selected_rand <- filtered_data$data$h_mam[filtered_data$data$metric=="rand"]
		
		if (length(selected_metric) > 0) {
		
		
		
			p <- wilcox.test(selected_metric, selected_rand, conf.int=TRUE)$p.value
			message(p)
			message(median(selected_metric) - median(selected_rand))

			row <- c(min_multiplier, max_multiplier, min, max, topMetric, length(selected_metric), length(selected_rand), median(selected_metric), median(selected_rand), median(selected_metric) - median(selected_rand), 
					p, filtered_data$omega_min, filtered_data$omega_max, filtered_data$phi_min, filtered_data$phi_max)

			rows <- append(rows, row)
		}
	}
	

	table <- matrix(rows, ncol = 15, byrow=TRUE)
	colnames(table)<-c("min_p", "max_p", "min", "max", "metric", "metric_n", "rand_n", "median", "median_rand", "median_diff", "p", "o_min", "o_max", "p_min", "p_max")
	table <- as.data.frame(table, stringsAsFactors=FALSE) 
	return(table)
}

jabref_intervals <- checkEveryInterval(jabref_loaded)
teammates_intervals <- checkEveryInterval(teammates_loaded)
argouml_intervals <- checkEveryInterval(argouml_loaded)
ant_intervals <- checkEveryInterval(ant_loaded)
lucene_intervals <- checkEveryInterval(lucene_loaded)

# make a plot of the interval data
plot(median_diff~max, data=jabref_intervals, type="n", xaxt="n", las=2, ylim=c(0, 0.6), xlab="Initial Set Size Range", ylab="Harmonic Mean Performance Median Difference")
x_axis <- c(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50)
axis(1, at=x_axis, labels=x_axis)

lines(median_diff~max, data=jabref_intervals, lty=1)
lines(median_diff~max, data=teammates_intervals, lty=2)
lines(median_diff~max, data=argouml_intervals, lty=3)
lines(median_diff~max, data=ant_intervals, lty=4)
lines(median_diff~max, data=lucene_intervals, lty=5)

legend("topright", legend=c("JabRef", "TeamMates", "ArgoUML", "Ant", "Lucene" ), lty=c(1,2,3,4,5), cex=1.0)
#title("Best Metric and Random Metric Median Differences")

# write the interval data for further analysis
write.table(jabref_intervals, file="jabref_intervals.csv", sep="\t", row.names = F)
write.table(teammates_intervals, file="teammates_intervals.csv", sep="\t", row.names = F)
write.table(argouml_intervals, file="argouml_intervals.csv", sep="\t", row.names = F)
write.table(ant_intervals, file="ant_intervals.csv", sep="\t", row.names = F)
write.table(lucene_intervals, file="lucene_intervals.csv", sep="\t", row.names = F)

# print the means of all the median diffs
mean(as.numeric(jabref_intervals$median_diff))
mean(as.numeric(teammates_intervals$median_diff))
mean(as.numeric(argouml_intervals$median_diff))
mean(as.numeric(ant_intervals$median_diff))
mean(as.numeric(lucene_intervals$median_diff))


####################################################################################################################
# investigate 20-35 interval for jabref, teammates, argouml and ant
jabref_filtered <- invisible(analyzeSystem(0.01, jabref_loaded, 20, 35))
teammates_filtered <- invisible(analyzeSystem(0.01, teammates_loaded, 20, 35))
argouml_filtered <- invisible(analyzeSystem(0.01, argouml_loaded, 20, 35))
ant_filtered <- invisible(analyzeSystem(0.01, ant_loaded, 20, 35))

jtaa_filtered <- rbind(jabref_filtered$data, teammates_filtered$data)
jtaa_filtered <- rbind(jtaa_filtered, argouml_filtered$data)
jtaa_filtered <- rbind(jtaa_filtered, ant_filtered$data)

# we need to selectrandomized set of data for each metric so that none is too dominant
selected_data <- jtaa_filtered
names = levels(selected_data$metric)
random_data <- NULL
for (i in 1:length(names)) {
	message(names[i])
	metric_data <- selected_data[selected_data$metric == names[i],]
	metric_data <- randomSample(metric_data)
	message(toString(summary(metric_data$system)))
	random_data <- rbind(random_data, metric_data)
}
selected_data <- random_data

# function for doing metrics boxplots on different columns
boxplotMetrics <- function(a_data, a_col) {
	a_data <- renameMetrics(a_data)
	var <- eval(substitute(a_col), a_data)
	var.name <- substitute(var)
	
	ordered_metrics <- with(a_data, reorder(metric, -var.name, FUN=median))
	oldpar = par(mar=c(5,5,2,1))
	boxplot(var~ordered_metrics, las=2)
	par(oldpar)
}

boxplotMetrics(selected_data, h_mam)
title(ylab="Harmonic Mean Performance")

lucene_filtered <- invisible(analyzeSystem(0.01, lucene_loaded, 20, 35))
boxplotMetrics(lucene_filtered$data, h_mam)
title(ylab="Harmonic Mean Performance")

boxplotMetrics(selected_data, ap)
title(ylab="Automatic Mapping Performance")



####################################################################################################################
# compute the best metrics in the 20-35 interval

computeFinalInterval <- function(a_data) {

	best_percent = 0.01
	rows <- NULL

	min <- 20
	max <- 35

	min_multiplier <- min / a_data$totalMapped[1]
	max_multiplier <- max / a_data$totalMapped[1]

	filtered_data <- invisible(analyzeSystem(best_percent, a_data, min, max))

	ordered_metrics <- with(filtered_data$data, reorder(metric, -h_mam, FUN=median))

	topMetric <- levels(ordered_metrics)[1]
	message(topMetric)

	selected_metric <- filtered_data$data$h_mam[filtered_data$data$metric==topMetric]
	selected_rand <- filtered_data$data$h_mam[filtered_data$data$metric=="rand"]
		
	if (length(selected_metric) > 0) {

		
		p <- wilcox.test(selected_metric, selected_rand, conf.int=TRUE)$p.value
		message(p)
		message(median(selected_metric) - median(selected_rand))

		row <- c(min_multiplier, max_multiplier, min, max, topMetric, length(selected_metric), length(selected_rand), median(selected_metric), median(selected_rand), median(selected_metric) - median(selected_rand), 
				p, filtered_data$omega_min, filtered_data$omega_max, filtered_data$phi_min, filtered_data$phi_max)

		rows <- append(rows, row)
	}

	table <- matrix(rows, ncol = 15, byrow=TRUE)
	colnames(table)<-c("min_p", "max_p", "min", "max", "metric", "metric_n", "rand_n", "median", "median_rand", "median_diff", "p", "o_min", "o_max", "p_min", "p_max")
	table <- as.data.frame(table, stringsAsFactors=FALSE) 
	return(table)
}


jabref_finalInterval <- computeFinalInterval(jabref_loaded)
teammates_finalInterval <- computeFinalInterval(teammates_loaded)
argouml_finalInterval <- computeFinalInterval(argouml_loaded)
ant_finalInterval <- computeFinalInterval(ant_loaded)
lucene_finalInterval <- computeFinalInterval(lucene_loaded)

jabref_finalInterval$median_rand
ant_finalInterval$median_diff
ant_finalInterval$p
ant_finalInterval$metric


