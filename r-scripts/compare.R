
rm(list=ls(all=TRUE))
library(coin)
setwd("C:/hObbE/projects/coding/research/s4rdm3x/r-scripts")

source("functions/getFilteredData.R")
source("functions/loadData.R")

# comparison of individual metrics

d_1 <- getFilteredData(1.00, 250, loadData("2018-10-29_own_metrics/jabref_37_1/bccc.csv"))
d_2 <- getFilteredData(1.00, 250, loadData("180921/jabref_37_1/bccc.csv"))

d_1 <- getFilteredData(0.1, 25, loadData("lucene/rand.csv"))
d_2 <- getFilteredData(0.1, 25, loadData("lucene/linecount.csv"))


d_1$phi_min
d_1$phi_max
d_2$phi_min
d_2$phi_max

d_1$omega_min
d_1$omega_max
d_2$omega_min
d_2$omega_max



d_1$data$metric = "bccc_new"
d_2$data$metric = "bccc_old"

nrow(d_1$data)
nrow(d_2$data)
summary(d_1$data)
summary(d_2$data)

data <- rbind(d_1$data, d_2$data)
summary(data)
boxplot(ap~metric, data=data)
boxplot(mr~metric, data=data)
boxplot(mp~metric, data=data)
boxplot(mappingPercent~metric, data=data)
boxplot(initialClustered~metric, data=data)
boxplot(h_mam~metric, data=data)
plot(h_mam~phi, data=d_1$data)
plot(h_mam~phi, data=d_2$data)


wilcox.test(d_1$data$ap, d_2$data$ap, conf.int=TRUE)
median(d_1$data$ap) - median(d_2$data$ap)
wilcox_test(data$ap~data$metric)

wilcox.test(d_1$data$h_mam, d_2$data$h_mam, conf.int=TRUE)
median(d_1$data$h_mam) - median(d_2$data$h_mam)
wilcox_test(data$h_mam~data$metric)

# compute effect size r
#https://stats.stackexchange.com/questions/133077/effect-size-to-wilcoxon-signed-rank-test
abs(statistic(wilcox_test(data$ap~data$metric)) / sqrt(nrow(data)))


# load all




all <- getFilteredData(0.001, 25, loadData("180921/argouml/all.csv"))
oldall <- getFilteredData(0.1, 25, loadData("180921/jabref_37_1/all.csv"))
all <- getFilteredData(0.001, 25, loadData("180921/teammates/all.csv"))
all <- getFilteredData(0.001, 25, loadData("180921/ant/all.csv"))
all <- getFilteredData(0.001, 25, loadData("180921/lucene/all.csv"))
all <- getFilteredData(0.001, 25, loadData("180921/sweethome3d/all.csv"))

# icsa2019 own metrics
icsa2019_rand <- getFilteredData(0.1, 25, loadData("ICSA2019/jabref_37_1/rand.csv")) 
icsa2019_rand$phi_min
icsa2019_rand$phi_max
icsa2019_rand$omega_min
icsa2019_rand$omega_max

jabref_loaded <- loadData("ICSA2019/jabref_37_1/all.csv")
teammates_loaded <- loadData("ICSA2019/teammates/all.csv")
sweethome3d_loaded <- loadData("ICSA2019/sweethome3d/all.csv")
argouml_loaded <- loadData("ICSA2019/argouml/all.csv")
argouml_old_loaded <- loadData("180921/argouml/all.csv")
ant_loaded <- loadData("ICSA2019/ant/all.csv")
lucene_loaded <- loadData("ICSA2019/lucene/all.csv")

nrow(sweethome3d_loaded)
nrow(lucene_loaded)


# min and max relative number of hypothetical entities
best_percent = 0.01
min_multiplier = 0
max_multiplier = 3
jabref_filtered <- invisible(analyzeSystem(best_percent, jabref_loaded, 6*min_multiplier, 6*max_multiplier))
teammates_filtered <- invisible(analyzeSystem(best_percent, teammates_loaded, 15*min_multiplier, 15*max_multiplier))
sweethome3d_filtered <- invisible(analyzeSystem(best_percent, sweethome3d_loaded, 9*min_multiplier, 9*max_multiplier))
argouml_filtered <- invisible(analyzeSystem(best_percent, argouml_loaded, 19*min_multiplier, 19*max_multiplier))
ant_filtered <- invisible(analyzeSystem(best_percent, ant_loaded, 16*min_multiplier, 16*max_multiplier))
lucene_filtered <- invisible(analyzeSystem(best_percent, lucene_loaded, 7*min_multiplier, 7*max_multiplier))

# absolute min and max
best_percent = 0.001
min = 0
max = 100
jabref_filtered <- invisible(analyzeSystem(best_percent, jabref_loaded, min, max))
teammates_filtered <- invisible(analyzeSystem(best_percent, teammates_loaded, min, max))
sweethome3d_filtered <- invisible(analyzeSystem(best_percent, sweethome3d_loaded, min, max))
argouml_filtered <- invisible(analyzeSystem(best_percent, argouml_loaded, min, max))
ant_filtered <- invisible(analyzeSystem(best_percent, ant_loaded, min, max))
lucene_filtered <- invisible(analyzeSystem(best_percent, lucene_loaded, min, max))

summary(jabref_loaded)
summary(teammates_loaded)
summary(sweethome3d_loaded)
summary(argouml_loaded)
summary(ant_loaded)
summary(lucene_loaded)

selected_data <- sweethome3d_loaded[sweethome3d_loaded$mappingPercent <= 0.05,]
ordered_metrics <- with(selected_data, reorder(metric, -h_mam, FUN=median))
boxplot(h_mam~ordered_metrics, data=selected_data, las=2)
title(system)

write.table(aggregate(selected_data$h_mam~selected_data$metric, FUN=median), file="out.csv", sep="\t", row.names = F)


write.table(total_datas, file="total_datas_size_0-0.05_bp0.001.csv", sep="\t")
median_diff_p_columns <- c("JabRef_37_1_median_diff_p", "TeamMates_median_diff_p", "sweethome3d_median_diff_p", "ArgoUML_median_diff_p", "ant_median_diff_p", "lucene_median_diff_p")
median_columns <- c("JabRef_37_1_median", "TeamMates_median", "sweethome3d_median", "ArgoUML_median", "ant_median", "lucene_median")


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
sweethome3d_intervals <- checkEveryInterval(sweethome3d_loaded)
argouml_intervals <- checkEveryInterval(argouml_loaded)
ant_intervals <- checkEveryInterval(ant_loaded)
lucene_intervals <- checkEveryInterval(lucene_loaded)

write.table(lucene_intervals, file="intervals.csv", sep="\t", row.names = F)



plot(median_diff~max, data=jabref_intervals, type="n", las=2, ylim=c(0, 0.6), xlab="Range", ylab="Median Diff")
lines(median_diff~max, data=jabref_intervals, lty=2)
lines(median_diff~max, data=teammates_intervals, lty=3)
lines(median_diff~max, data=argouml_intervals, lty=4)
lines(median_diff~max, data=ant_intervals, lty=5)
lines(median_diff~max, data=lucene_intervals, lty=6)
#lines(median_diff~max, data=sweethome3d_intervals)
legend("topright", legend=c("JabRef", "TeamMates", "ArgoUML", "Ant", "Lucene" ), lty=c(2,3,4,5,6), cex=1.0)

title("Best Metric and Random Metric Median Differences")

nrow(jabref_loaded)+nrow(teammates_loaded)+nrow(argouml_loaded)+nrow(ant_loaded)+nrow(lucene_loaded)

plot(median~max, data=jabref_intervals, type="n", las=2, ylim=c(0, 1.0))
lines(median~max, data=jabref_intervals)
lines(median~max, data=teammates_intervals)
lines(median~max, data=argouml_intervals)
lines(median~max, data=ant_intervals)
lines(median~max, data=lucene_intervals)
lines(median~max, data=sweethome3d_intervals)

plot(median~max, data=jabref_intervals, type="n", las=2, ylim=c(0, 1.0))
lines(median_rand~max, data=jabref_intervals)
lines(median_rand~max, data=teammates_intervals)
lines(median_rand~max, data=argouml_intervals)
lines(median_rand~max, data=ant_intervals)
lines(median_rand~max, data=lucene_intervals)
lines(median_rand~max, data=sweethome3d_intervals)

plot(median~max, data=jabref_intervals, type="n", las=2, ylim=c(0, 1000.0))
lines(metric_n~max, data=jabref_intervals)
lines(rand_n~max, data=jabref_intervals)

lines(metric_n~max, data=argouml_intervals)
lines(metric_n~max, data=ant_intervals)
lines(metric_n~max, data=lucene_intervals)
lines(metric_n~max, data=sweethome3d_intervals)





plot(metric_n~min, data=jabref_intervals, type="n", las=2, ylim=c(0, 1500))
lines(metric_n~min, data=jabref_intervals)
lines(metric_n~min, data=teammates_intervals)
lines(metric_n~min, data=argouml_intervals)
lines(metric_n~min, data=ant_intervals)
lines(metric_n~min, data=lucene_intervals)
lines(metric_n~min, data=sweethome3d_intervals)

sweethome3d_intervals$metric_n

sum(intervals$median_diff)
intervals$median_diff

nrow(jabref_intervals)


source("functions/getFilteredData.R")
source("functions/util.R")
source("functions/loadData.R")

total_datas<-NULL
for (i in 0:15) {
#for (i in 0:10) {
# min and max relative number of concrete entities
i
best_percent = 0.01
min_multiplier = (0.00 + i * 0.002) * 100
max_multiplier = (0.00 + (i + 1) * 0.002) * 100

#min_multiplier = (0.0 + i * 0.005) * 1
#max_multiplier = (0.0 + (i + 1) * 0.005) * 1

#jabref_filtered <- invisible(analyzeSystem(best_percent, jabref_loaded, 1017*min_multiplier, 1017*max_multiplier))
#teammates_filtered <- invisible(analyzeSystem(best_percent, teammates_loaded, 779*min_multiplier, 779*max_multiplier))
#sweethome3d_filtered <- invisible(analyzeSystem(best_percent, sweethome3d_loaded, 167*min_multiplier, 167*max_multiplier))
#argouml_filtered <- invisible(analyzeSystem(best_percent, argouml_loaded, 767*min_multiplier, 767*max_multiplier))
#ant_filtered <- invisible(analyzeSystem(best_percent, ant_loaded, 468*min_multiplier, 468*max_multiplier))
#lucene_filtered <- invisible(analyzeSystem(best_percent, lucene_loaded, 514*min_multiplier, 514*max_multiplier))

jabref_filtered <- invisible(analyzeSystem(best_percent, jabref_loaded, 6*min_multiplier, 6*max_multiplier))
teammates_filtered <- invisible(analyzeSystem(best_percent, teammates_loaded, 15*min_multiplier, 15*max_multiplier))
sweethome3d_filtered <- invisible(analyzeSystem(best_percent, sweethome3d_loaded, 9*min_multiplier, 9*max_multiplier))
argouml_filtered <- invisible(analyzeSystem(best_percent, argouml_loaded, 19*min_multiplier, 19*max_multiplier))
ant_filtered <- invisible(analyzeSystem(best_percent, ant_loaded, 16*min_multiplier, 16*max_multiplier))
lucene_filtered <- invisible(analyzeSystem(best_percent, lucene_loaded, 7*min_multiplier, 7*max_multiplier))


# merge all the data to one table
total_data <- rbind(jabref_filtered$data, teammates_filtered$data)
total_data <- rbind(total_data, sweethome3d_filtered$data)
total_data <- rbind(total_data, argouml_filtered$data)
total_data <- rbind(total_data, ant_filtered$data)
total_data <- rbind(total_data, lucene_filtered$data)

total_data$initial_set <- min_multiplier
message(min_multiplier)
message(toString(summary(total_data$system)))

if (is.null(total_datas)) {
	total_datas <- total_data
} else {
	total_datas <- rbind(total_datas, total_data)
}

}
addInitialDistributionColumn <- function(a_data) {
	a_data$gini <- NA
	for (rIx in 1:nrow(a_data)) {
		a_data$gini[rIx] <- initialDistributionToGini(a_data$initialDistribution[rIx])
	}
	return(a_data)
}


apply(jabref_filtered$data, 1, initialDistributionToGini)
jabref_filtered$data$gini <- initialDistributionToGini(jabref_filtered$data$initialDistribution)
gini_data <- addInitialDistributionColumn(jabref_filtered$data)

summary(gini_data)


jabref_filtered$data$gini

#plot(Lc(number_of_concrete_elements),col="darkred",lwd=2)


printOmegaPhi(jabref_filtered)
printOmegaPhi(teammates_filtered)
printOmegaPhi(argouml_filtered)
printOmegaPhi(sweethome3d_filtered)
printOmegaPhi(ant_filtered)
printOmegaPhi(lucene_filtered)


# investigate 20-35 interval for jabref, teammates, argouml and ant

jabref_filtered <- invisible(analyzeSystem(best_percent, jabref_loaded, 20, 35))
jabref_filtered$data <- addInitialDistributionColumn(jabref_filtered$data)

teammates_filtered <- invisible(analyzeSystem(best_percent, teammates_loaded, 20, 35))
teammates_filtered$data <- addInitialDistributionColumn(teammates_filtered$data)

argouml_filtered <- invisible(analyzeSystem(best_percent, argouml_loaded, 20, 35))
argouml_filtered$data <- addInitialDistributionColumn(argouml_filtered$data)

ant_filtered <- invisible(analyzeSystem(best_percent, ant_loaded, 20, 35))
ant_filtered$data <- addInitialDistributionColumn(ant_filtered$data)

sweethome3d_filtered <- invisible(analyzeSystem(best_percent, sweethome3d_loaded, 20, 35))
sweethome3d_filtered$data <- addInitialDistributionColumn(sweethome3d_filtered$data)

lucene_filtered <- invisible(analyzeSystem(best_percent, lucene_loaded, 20, 35))
lucene_filtered$data <- addInitialDistributionColumn(lucene_filtered$data)

boxplotMetrics(jabref_filtered$data, h_mam)
boxplotMetrics(teammates_filtered$data, h_mam)
boxplotMetrics(argouml_filtered$data, h_mam)
boxplotMetrics(ant_filtered$data, h_mam)

boxplotMetrics(lucene_filtered$data, h_mam)
boxplotMetrics(sweethome3d_filtered$data, h_mam)

# merge all the data to one table
total_data <- rbind(jabref_filtered$data, teammates_filtered$data)
total_data <- rbind(total_data, argouml_filtered$data)
total_data <- rbind(total_data, ant_filtered$data)

total_datas <- total_data

selected_data <- total_data
selected_data$system <- factor(selected_data$system)
selected_data <- randomSample(selected_data)

summary(total_data)

# we need to resample every metric so that they have an even distribution for each system
names = levels(total_datas$metric)

random_data <- NULL
for (i in 1:length(names)) {
	message(names[i])
	metric_data <- selected_data[selected_data$metric == names[i],]
	metric_data <- randomSample(metric_data)
	message(toString(summary(metric_data$system)))
	random_data <- rbind(random_data, metric_data)
}

boxplotMetrics <- function(a_data, a_col) {
	a_data <- renameMetrics(a_data)
	var <- eval(substitute(a_col), a_data)
	var.name <- substitute(var)
	
	ordered_metrics <- with(a_data, reorder(metric, -var.name, FUN=median))
	oldpar = par(mar=c(5,5,2,1))
	boxplot(var~ordered_metrics, las=2)
	par(oldpar)
}

selected_data <- random_data
boxplotMetrics(selected_data, h_mam)
boxplotMetrics(selected_data, h_am)
boxplotMetrics(selected_data, ap)
boxplotMetrics(selected_data, mp)
boxplotMetrics(selected_data, mr)

selected_data <- random_data
selected_number_of_children <- selected_data[selected_data$metric == "NumberOfChildren",]
selected_linecount <- selected_data[selected_data$metric == "linecount",]
selected_rand <- selected_data[selected_data$metric == "rand",]

wilcox.test(selected_number_of_children$h_mam, selected_rand$h_mam, conf.int=TRUE)
median(selected_number_of_children$h_mam) - median(selected_rand$h_mam)

wilcox.test(selected_number_of_children$h_mam, selected_linecount$h_mam, conf.int=TRUE)
median(selected_number_of_children$h_mam) - median(selected_linecount$h_mam)

# display jabref
system_data <- jabref_filtered$data
system_data <- teammates_filtered$data
system_data <- argouml_filtered$data
system_data <- ant_filtered$data
system_data <- lucene_filtered$data
system_data <- sweethome3d_filtered$data



boxplotMetrics(lucene_filtered$data, ap)

# end investigation



summary(total_data$system)

total_data <- NULL
total_data <- mergeTablesByRow(jabref_filtered$data, teammates_filtered$data)
total_data <- rbind(jabref_filtered$data, teammates_filtered$data)

summary(jabref_filtered$data)
summary(teammates_filtered$data)
summary(total_data)

nrow(jabref_filtered$data)+nrow(teammates_filtered$data)
nrow(total_datas)
summary(total_datas$system)


#plot(median_p~initial_set, data=total_medians[total_medians$metric=="fanout",])
#plot(median_p~initial_set, data=total_medians[total_medians$metric=="fanin",])
#plot(median_p~initial_set, data=total_medians[total_medians$metric=="CouplingOut",])
#plot(median_p~initial_set, data=total_medians[total_medians$metric=="CouplingIn",])
#plot(median_p~initial_set, data=total_medians[total_medians$metric=="NumberOfParents",])
#plot(median_p~initial_set, data=total_medians[total_medians$metric=="NumberOfChildren",])
#plot(median_p~initial_set, data=total_medians[total_medians$metric=="NumberOfFields",])
#plot(median_p~initial_set, data=total_medians[total_medians$metric=="NumberOfMethods",])
#plot(median_p~initial_set, data=total_medians[total_medians$metric=="ByteCodeCyclomaticComplexity",])
#plot(median_p~initial_set, data=total_medians[total_medians$metric=="LackOfCohesionOfMethods_HS",])






system = "JabRef_37_1"
system = "TeamMates"
system = "ant"
system = "ArgoUML"
system = "lucene"
system = "sweethome3d"

selected_data <- total_datas[total_datas$system==system & total_datas$mappingPercent <= 0.05,]
selected_data <- total_datas[total_datas$system==system,]
ordered_metrics <- with(selected_data, reorder(metric, -h_mam, FUN=median))
boxplot(h_mam~ordered_metrics, data=selected_data, las=2)
title(system)

write.table(aggregate(selected_data$h_mam~selected_data$metric, FUN=median), file="out.csv", sep="\t", row.names = F)

metric <- "NumberOfChildren" # JabRef, ant
metric <- "NumberOfParents" # TeamMates, ant
metric <- "NumberOfFields" # lucene
metric <- "CouplingIn" # sweethome3d

selected_metric <- selected_data$h_mam[selected_data$metric==metric]
selected_rand <- selected_data$h_mam[selected_data$metric=="rand"]
wilcox.test(selected_metric, selected_rand, conf.int=TRUE)
median(selected_metric) - median(selected_rand)


metric <- "CouplingIn" # sweethome3d
metric <- "ByteCodeInstructions"
metric <- "fanin"

# for this one we need to sample randomly from each system so that we get an even distribution over the systems
selected_data <- randomSample(total_datas[total_datas$metric==metric,])
selected_data_rand <- randomSample(total_datas[total_datas$metric=="rand",])
selected_data_linecount <- randomSample(total_datas[total_datas$metric=="linecount",])

selected_data <- total_datas[total_datas$metric==metric & total_datas$system==system,]
selected_data_rand <- total_datas[total_datas$metric=="rand" & total_datas$system==system,]
selected_data_linecount <- total_datas[total_datas$metric=="linecount" & total_datas$system==system,]

selected_data <- total_datas[total_datas$metric==metric & total_datas$system!="sweethome3d" & total_datas$system!="lucene",]
selected_data$system <- factor(selected_data$system)
selected_data <- randomSample(selected_data)
selected_data_rand <- total_datas[total_datas$metric=="rand" & total_datas$system!="sweethome3d" & total_datas$system!="lucene",]
selected_data_rand$system <- factor(selected_data_rand$system)
selected_data_rand <- randomSample(selected_data_rand)
selected_data_linecount <- total_datas[total_datas$metric=="linecount" & total_datas$system!="sweethome3d" & total_datas$system!="lucene",]
selected_data_linecount$system <- factor(selected_data_linecount$system)
selected_data_linecount <- randomSample(selected_data_linecount)

nrow(selected_data)
summary(selected_data)
summary(selected_data$system)
summary(selected_data_rand$system)
summary(selected_data_linecount$system)


#stripchart(h_mam~initial_set, data=selected_data, las=2)

# prints the random set
#plot(median~initial_set, data=total_medians[total_medians$metric==metric ,], ylim=c(-1, 0.8), type="n")
#lines(median~initial_set, data=total_medians[total_medians$metric==metric ,])
#lines(median~initial_set, data=total_medians[total_medians$metric=="rand",])


medians_rand <- aggregate(selected_data_rand$h_mam~selected_data_rand$initial_set, FUN=median)
colnames(medians_rand) <- c("initial_set", "median")

medians_linecount <- aggregate(selected_data_linecount$h_mam~selected_data_linecount$initial_set, FUN=median)
colnames(medians_linecount) <- c("initial_set", "median")


medians <- aggregate(selected_data$h_mam~selected_data$initial_set, FUN='quantile')
medians_temp <- as.data.frame(medians$"selected_data$h_mam")
medians_temp$initial_set <- medians$"selected_data$initial_set"
medians <- medians_temp

colnames(medians) <- c("min", "lower", "median", "higher", "max", "initial_set")
plot(median~initial_set, data=medians, las=2, type="n", ylim=c(0, 1.0))
polygon(c(medians$initial_set, rev(medians$initial_set)), c(medians$max, rev(medians$min)), col = "grey90", border = NA)
polygon(c(medians$initial_set, rev(medians$initial_set)), c(medians$higher, rev(medians$lower)), col = "grey70", border = NA)

lines(median~initial_set, data=medians)

lines(median~initial_set, data=medians_rand, lty="dotted")
#lines(median~initial_set, data=medians_linecount, lty="dashed")
title(metric)


#lines(lower~initial_set, data=medians)
#lines(higher~initial_set, data=medians)
#lines(min~initial_set, data=medians)
#lines(max~initial_set, data=medians)



wilcox.test(medians$median, medians_rand$median, conf.int=TRUE)
median(medians$median) - median(medians_rand$median)

wilcox.test(medians$median, medians_linecount$median, conf.int=TRUE)
median(medians$median) - median(medians_linecount$median)

wilcox.test(medians_rand$median, medians_linecount$median, conf.int=TRUE)



write.table(total_medians, file="totalmedians.csv", sep="\t")

write.table(total_datas, file="total_datas_0-10_001.csv", sep="\t")


aggregate(total[,c("JabRef_37_1_median_diff_p", "lucene_median_diff_p")], by=list(total$Row.names))

aggregate(total, by=list("JabRef_37_1_median_diff_p", "lucene_median_diff_p"), FUN = median)





plot(h_mam~mappingPercent, data=lucene_filtered$data[lucene_filtered$data$metric == "rand",])

invisible(analyzeSystem(best_percent, argouml_old_loaded, 19))

rand_loaded <- all_loaded[jabref_all_loaded$metric == "rand",];


plot(omega~phi, data = rand_loaded)

rand_omegaphi <- getOmegaPhi(.001, rand_loaded)
rand_omegaphi$omega_max - rand_omegaphi$omega_min
rand_omegaphi$phi_max - rand_omegaphi$phi_min



all_filtered <- getFilteredData_OmegaPhi(10, 25, rand_omegaphi, all_loaded)
metrics <- with(all_filtered$data, reorder(metric, -h_mam, FUN=median))
oldpar = par(mar=c(20,5,1,1))
boxplot(h_mam~metrics, data=all_filtered$data, las=2)

metrics
nrow(all$data)
median(all$data$h_mam[all$data$metric == "Rank"])


compare_data <- all$data[all$data$metric == "rand" | all$data$metric == "fanout_linecount",]
nrow(compare_data)
wilcox_test(compare_data$h_mam~compare_data$metric)

statistic(wilcox_test(compare_data$h_mam~compare_data$metric))

abs(statistic(wilcox_test(compare_data$h_mam~compare_data$metric)) / sqrt(nrow(compare_data)))

median(compare_data$h_mam[compare_data$metric == "rand"]) - median(compare_data$h_mam[compare_data$metric == "fanout_linecount"])


hist(all$data$h_mam[all$data$metric == "fanout"])

length(unique(all$data$metric))
nrow(all$data)
summary(all$data)
all$phi_min
all$phi_max

all$omega_min
all$omega_max

plot(ap~phi, data=all$data)
plot(ap~omega, data=all$data)

oldpar = par(mar=c(20,5,1,1))
metrics <- with(all$data, reorder(metric, -ap, FUN=median))
boxplot(ap~metrics, data=all$data, las=2)

metrics <- with(all$data, reorder(metric, -mr, FUN=median))
boxplot(mr~metrics, data=all$data, las=2)

metrics <- with(all$data, reorder(metric, -mp, FUN=median))
boxplot(mp~metrics, data=all$data, las=2)

metrics <- with(all$data, reorder(metric, -h_mam, FUN=median))
boxplot(h_mam~metrics, data=all$data, las=2)

metrics <- with(oldall$data, reorder(metric, -h_mam, FUN=median))
boxplot(h_mam~metrics, data=oldall$data, las=2)


boxplot(h_am~metric, data=all$data, las=2)

par(oldpar)
