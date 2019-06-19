rm(list=ls(all=TRUE))
library(coin)
library(caTools)
#install.packages("export")
library(export)

setwd("C:/hObbE/projects/coding/research/s4rdm3x/r-scripts/SAEroCon19")

loadData <- function(a_csvFile) {
	data <- read.csv(a_csvFile, head=TRUE, sep="\t")

	data <- data[data$initialClustered < data$totalMapped, ]

	data$performance = (data$totalAutoClustered - data$totalAutoWrong) / (data$totalMapped)
	data$precision = ifelse(data$totalAutoClustered == 0, 0, (data$totalAutoClustered - data$totalAutoWrong) / (data$totalAutoClustered))
	data$recall = ifelse(data$totalMapped - data$initialClustered - data$totalManuallyClustered == 0, 0, (data$totalAutoClustered - data$totalAutoWrong) / (data$totalMapped - data$initialClustered - data$totalManuallyClustered))
	
	# harmonic means for the performance metrics
	#data$h_mam = ifelse(data$ap == 0 | data$mp == 0 | data$mr == 0, 0, 3*data$ap*data$mr*data$mp/(data$ap*data$mr + data$ap*data$mp + data$mp*data$mr))
	data$f1 = ifelse(data$precision == 0 | data$recall == 0, 0, 2*data$precision*data$recall/(data$precision+data$recall))
	return(data)
}

getOmegaPhi <- function(a_best_percent, a_data) {
	data_filtered = a_data
	data_best = data_filtered[data_filtered$performance > quantile(data_filtered$performance, c(1.0 - a_best_percent), names=FALSE),]
	out = list()
	out$omega_min <- min(data_best$omega)
	out$omega_max <- max(data_best$omega)
	out$phi_min <- min(data_best$phi)
	out$phi_max <- max(data_best$phi)
	return(out)
}

getFilteredData <- function(a_best_percent, a_data) {
	omegaphi = getOmegaPhi(a_best_percent, a_data)
	out = list()
	out$omega_min <- omegaphi$omega_min
	out$omega_max <- omegaphi$omega_max
	out$phi_min <- omegaphi$phi_min
	out$phi_max <- omegaphi$phi_max

	out$data <- a_data[a_data$phi >= out$phi_min & a_data$phi <= out$phi_max & a_data$omega >= out$omega_min & a_data$omega <= out$omega_max,]

	return(out)
}

plotRunningMedian <- function(a_data, a_color, a_column) {
	data <- data.frame(a_data[[a_column]], a_data$mappingPercent)
	names(data)[1] = "thedata"
	names(data)[2] = "mappingPercent"
	data <- data[order(data$mappingPercent),]

	# this produces median
	y_lag <- runquantile(data$thedata, 1001, probs=c(0.5), align="center")

	lines(data$mappingPercent, y_lag, col=a_color)
}

plotRunningQuantiles <- function(a_data, a_color, a_column) {
	
	data <- data.frame(a_data[[a_column]], a_data$mappingPercent)
	names(data)[1] = "thedata"
	names(data)[2] = "mappingPercent"
	data <- data[order(data$mappingPercent),]

	y = runquantile(data$thedata, 1001, probs=c(0.25, 0.75), align="center")

	#lines(data$mappingPercent, y[,1], col=a_color)
	#lines(data$mappingPercent, y[,2], col=a_color)

	polygon(c(data$mappingPercent, rev(data$mappingPercent)),c(y[,1], rev(y[,2])), col=a_color, border=NA)
}

plotRunningData <- function(a_data1, a_data2, a_data3, a_title, a_column, a_legendPos) {
	# Draw an empty plot
	plot(5, 5, type="n", yaxt="n", xaxt="n", ann=FALSE, xlim=c(0, 1.0), ylim = c(0,1))

	# add the x axis
	axis(side = 1, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01)

	# Add a title
	title(a_title)

	# colors from: http://ksrowell.com/blog-visualizing-data/2012/02/02/optimal-colors-for-graphs/
	c1 <- rgb(57, 106, 177, max = 255, alpha = 255)
	c1_a <- rgb(114, 147, 203, max = 255, alpha = 80)

	c2 <- rgb(218, 124, 48, max = 255, alpha = 255)
	c2_a <- rgb(225, 151, 76, max = 255, alpha = 80)

	c3 <- rgb(62, 150, 81, max = 255, alpha = 255)
	c3_a <- rgb(132, 186, 91, max = 255, alpha = 80)

	plotRunningQuantiles(a_data1, c1_a, a_column)
	plotRunningQuantiles(a_data2, c2_a, a_column)
	plotRunningQuantiles(a_data3, c3_a, a_column)

	plotRunningMedian(a_data1, c1, a_column)
	plotRunningMedian(a_data2, c2, a_column)
	plotRunningMedian(a_data3, c3, a_column)

	legend(a_legendPos, legend=c("NBC", "HuGMe", "HuGMe w. MM"),col=c(c1, c2, c3), lty=1)
}

plotBoxPlotsAllData <- function(a_column, a_title) {
	boxplot(
		nbc_jabref[[a_column]], hugme_jabref$data[[a_column]], hugme_manual_jabref$data[[a_column]],
		nbc_ant[[a_column]], hugme_ant$data[[a_column]], hugme_manual_ant$data[[a_column]],
		nbc_argouml[[a_column]], hugme_argouml$data[[a_column]], hugme_manual_argouml$data[[a_column]],
		nbc_lucene[[a_column]], hugme_lucene$data[[a_column]], hugme_manual_lucene$data[[a_column]],
		nbc_teammates[[a_column]], hugme_teammates$data[[a_column]], hugme_manual_teammates$data[[a_column]],
		nbc_sweethome3d[[a_column]], hugme_sweethome3d$data[[a_column]], hugme_manual_sweethome3d$data[[a_column]],

		main = a_title,
		at = rev(c(1, 2, 3, 5, 6, 7, 9, 10, 11, 13, 14, 15, 17, 18, 19, 21, 22, 23)),
		names = c("JabRef", "", "", "Ant", "", "", "A.Uml", "", "", "Lucene", "", "", "T.Mates", "", "", "S.H.3D", "", ""),
		las = 2,
		col = c(rgb(114, 147, 203, max = 255),rgb(225, 151, 76, max = 255), rgb(132, 186, 91, max = 255)),
		border = "black",
		horizontal = TRUE,
		notch = FALSE
	)
}

# load jabref
hugme_jabref_loaded <- loadData("new_data/hugme/jabref/hugme_jabref.csv")
hugme_jabref <- getFilteredData(0.1, hugme_jabref_loaded)
hugme_manual_jabref_loaded <- loadData("new_data/hugme_manual/jabref/hugme_manual_jabref.csv")
hugme_manual_jabref <- getFilteredData(0.1, hugme_manual_jabref_loaded)
nbc_jabref_loaded <- loadData("new_data/nbc/jabref/nbc_jabref.csv")
nbc_jabref <- nbc_jabref_loaded

#load ant
hugme_ant_loaded <- loadData("new_data/hugme/ant/hugme_ant.csv")
hugme_ant  <- getFilteredData(0.1, hugme_ant_loaded)
hugme_manual_ant_loaded <- loadData("new_data/hugme_manual/ant/hugme_manual_ant.csv")
hugme_manual_ant <- getFilteredData(0.1, hugme_manual_ant_loaded)
nbc_ant_loaded <- loadData("new_data/nbc/ant/nbc_ant.csv")
nbc_ant <- nbc_ant_loaded

#load argouml
hugme_argouml_loaded <- loadData("new_data/hugme/argouml/hugme_argouml.csv")
hugme_argouml<- getFilteredData(0.1, hugme_argouml_loaded)
hugme_manual_argouml_loaded <- loadData("new_data/hugme_manual/argouml/hugme_manual_argouml.csv")
hugme_manual_argouml<- getFilteredData(0.1, hugme_manual_argouml_loaded)
nbc_argouml_loaded <- loadData("new_data/nbc/argouml/nbc_argouml.csv")
nbc_argouml<- nbc_argouml_loaded

#load lucene
hugme_lucene_loaded <- loadData("new_data/hugme/lucene/hugme_lucene.csv")
hugme_lucene<- getFilteredData(0.1, hugme_lucene_loaded)
hugme_manual_lucene_loaded <- loadData("new_data/hugme_manual/lucene/hugme_manual_lucene.csv")
hugme_manual_lucene<- getFilteredData(0.1, hugme_manual_lucene_loaded)
nbc_lucene_loaded <- loadData("new_data/nbc/lucene/nbc_lucene.csv")
nbc_lucene<- nbc_lucene_loaded

#load teammates
hugme_teammates_loaded <- loadData("new_data/hugme/teammates/hugme_teammates.csv")
hugme_teammates<- getFilteredData(0.1, hugme_teammates_loaded)
hugme_manual_teammates_loaded <- loadData("new_data/hugme_manual/teammates/hugme_manual_teammates.csv")
hugme_manual_teammates<- getFilteredData(0.1, hugme_manual_teammates_loaded)
nbc_teammates_loaded <- loadData("new_data/nbc/teammates/nbc_teammates.csv")
nbc_teammates<- nbc_teammates_loaded

#sweethome3d
hugme_sweethome3d_loaded <- loadData("new_data/hugme/sweethome3d/hugme_sweethome3d.csv")
hugme_sweethome3d<- getFilteredData(0.1, hugme_sweethome3d_loaded)
hugme_manual_sweethome3d_loaded <- loadData("new_data/hugme_manual/sweethome3d/hugme_manual_sweethome3d.csv")
hugme_manual_sweethome3d<- getFilteredData(0.1, hugme_manual_sweethome3d_loaded)
nbc_sweethome3d_loaded <- loadData("new_data/nbc/sweethome3d/nbc_sweethome3d.csv")
nbc_sweethome3d<- nbc_sweethome3d_loaded


nrow(nbc_jabref_loaded) +
nrow(hugme_jabref_loaded) +
nrow(hugme_manual_jabref_loaded) +

nrow(nbc_ant_loaded) +
nrow(hugme_ant_loaded) +
nrow(hugme_manual_ant_loaded) +

nrow(nbc_argouml_loaded) +
nrow(hugme_argouml_loaded) +
nrow(hugme_manual_argouml_loaded) +

nrow(nbc_lucene_loaded) +
nrow(hugme_lucene_loaded) +
nrow(hugme_manual_lucene_loaded) +

nrow(nbc_teammates_loaded) +
nrow(hugme_teammates_loaded) +
nrow(hugme_manual_teammates_loaded) +

nrow(nbc_sweethome3d_loaded) +
nrow(hugme_sweethome3d_loaded) +
nrow(hugme_manual_sweethome3d_loaded)


# after filtering
nrow(nbc_jabref) +
nrow(hugme_jabref$data) +
nrow(hugme_manual_jabref$data) +

nrow(nbc_ant) +
nrow(hugme_ant$data) +
nrow(hugme_manual_ant$data) +

nrow(nbc_argouml) +
nrow(hugme_argouml$data) +
nrow(hugme_manual_argouml$data) +

nrow(nbc_lucene) +
nrow(hugme_lucene$data) +
nrow(hugme_manual_lucene$data) +

nrow(nbc_teammates) +
nrow(hugme_teammates$data) +
nrow(hugme_manual_teammates$data) +

nrow(nbc_sweethome3d) +
nrow(hugme_sweethome3d$data) +
nrow(hugme_manual_sweethome3d$data)




oldPar = par(oma=c(2,2,0,0),mar=c(0,0,2,0), mfrow=c(2,3))
oldPar = par(fig=c(0.075,0.365,0.55,1.0))
plotRunningData(nbc_jabref, hugme_jabref$data, hugme_manual_jabref$data, "JabRef", "performance", "topright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01)

oldPar = par(fig=c(0.39,0.68,0.55,1.0), new = TRUE)
plotRunningData(nbc_ant, hugme_ant$data, hugme_manual_ant$data, "Ant", "performance", "topright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01, labels=FALSE)

oldPar = par(fig=c(0.705,0.99,0.55,1.0), new = TRUE)
plotRunningData(nbc_argouml, hugme_argouml$data, hugme_manual_argouml$data, "ArgoUML", "performance", "topright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01, labels=FALSE)

# row 2
oldPar = par(fig=c(0.075,0.365,0.075,0.5125), new = TRUE)
plotRunningData(nbc_lucene, hugme_lucene$data, hugme_manual_lucene$data, "Lucene", "performance", "topright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01)

oldPar = par(fig=c(0.39,0.68,0.075,0.5125), new = TRUE)
plotRunningData(nbc_teammates, hugme_teammates$data, hugme_manual_teammates$data, "Teammates", "performance", "topright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01, labels=FALSE)

oldPar = par(fig=c(0.705,0.99,0.075,0.5125), new = TRUE)
plotRunningData(nbc_sweethome3d, hugme_sweethome3d$data, hugme_manual_sweethome3d$data, "Sweet Home 3D", "performance", "topright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01, labels=FALSE)

mtext(text="Initial Mapping Fraction",side=1,line=0,outer=TRUE)
mtext(text="performance",side=2,line=0,outer=TRUE)

graph2eps(file="p_all.eps", bg = "transparent", fallback_resolution = 600)

# F1 Score
oldPar = par(oma=c(2,2,0,0),mar=c(0,0,2,0), mfrow=c(2,3))
oldPar = par(fig=c(0.075,0.365,0.55,1.0))
plotRunningData(nbc_jabref, hugme_jabref$data, hugme_manual_jabref$data, "JabRef", "f1", "bottomright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01)

oldPar = par(fig=c(0.39,0.68,0.55,1.0), new = TRUE)
plotRunningData(nbc_ant, hugme_ant$data, hugme_manual_ant$data, "Ant", "f1", "bottomright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01, labels=FALSE)

oldPar = par(fig=c(0.705,0.99,0.55,1.0), new = TRUE)
plotRunningData(nbc_argouml, hugme_argouml$data, hugme_manual_argouml$data, "ArgoUML", "f1", "bottomright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01, labels=FALSE)

# row 2
oldPar = par(fig=c(0.075,0.365,0.075,0.5125), new = TRUE)
plotRunningData(nbc_lucene, hugme_lucene$data, hugme_manual_lucene$data, "Lucene", "f1", "bottomright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01)

oldPar = par(fig=c(0.39,0.68,0.075,0.5125), new = TRUE)
plotRunningData(nbc_teammates, hugme_teammates$data, hugme_manual_teammates$data, "Teammates", "f1", "bottomright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01, labels=FALSE)

oldPar = par(fig=c(0.705,0.99,0.075,0.5125), new = TRUE)
plotRunningData(nbc_sweethome3d, hugme_sweethome3d$data, hugme_manual_sweethome3d$data, "Sweet Home 3D", "f1", "bottomright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01, labels=FALSE)

mtext(text="Initial Mapping Fraction",side=1,line=0,outer=TRUE)
mtext(text="Precision and Recall F1 score",side=2,line=0,outer=TRUE)

graph2eps(file="f1_all.eps", bg = "transparent", fallback_resolution = 600)

par(oldPar)


# Precsion Score
oldPar = par(oma=c(2,2,0,0),mar=c(0,0,2,0), mfrow=c(2,3))
oldPar = par(fig=c(0.075,0.365,0.55,1.0))
plotRunningData(nbc_jabref, hugme_jabref$data, hugme_manual_jabref$data, "JabRef", "precision", "bottomright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01)

oldPar = par(fig=c(0.39,0.68,0.55,1.0), new = TRUE)
plotRunningData(nbc_ant, hugme_ant$data, hugme_manual_ant$data, "Ant", "precision", "bottomright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01, labels=FALSE)

oldPar = par(fig=c(0.705,0.99,0.55,1.0), new = TRUE)
plotRunningData(nbc_argouml, hugme_argouml$data, hugme_manual_argouml$data, "ArgoUML", "precision", "bottomright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01, labels=FALSE)

# row 2
oldPar = par(fig=c(0.075,0.365,0.075,0.5125), new = TRUE)
plotRunningData(nbc_lucene, hugme_lucene$data, hugme_manual_lucene$data, "Lucene", "precision", "bottomright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01)

oldPar = par(fig=c(0.39,0.68,0.075,0.5125), new = TRUE)
plotRunningData(nbc_teammates, hugme_teammates$data, hugme_manual_teammates$data, "Teammates", "precision", "bottomright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01, labels=FALSE)

oldPar = par(fig=c(0.705,0.99,0.075,0.5125), new = TRUE)
plotRunningData(nbc_sweethome3d, hugme_sweethome3d$data, hugme_manual_sweethome3d$data, "Sweet Home 3D", "precision", "bottomright")
axis(side = 2, at=c(0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0), tck=-.01, labels=FALSE)

mtext(text="Initial Mapping Fraction",side=1,line=0,outer=TRUE)
mtext(text="Precision",side=2,line=0,outer=TRUE)




plotBoxPlotsAllData("Performance", "Performance Comparison")
plotBoxPlotsAllData("f1", "F1 Comparison")

mergeColumn <- function(a_target, a_data, a_dataCol, a_group, a_groupCol) {
	if (length(a_target[[a_dataCol]]) == 0) {
		d = c(a_data[[a_dataCol]]);
		group = rep(a_group, length(a_data[[a_dataCol]]))
	} else {
		d = append(a_target[[a_dataCol]], a_data[[a_dataCol]])
		group = append(a_target[[a_groupCol]], rep(a_group, length(a_data[[a_dataCol]])))
	}
	ret = data.frame(d, group)
	names(ret)[1] = a_dataCol
	names(ret)[2] = a_groupCol
	
	ret[[a_groupCol]] = as.factor(ret[[a_groupCol]])
	ret
}

wilcoxTest <- function(a_dataSet1, a_dataSet2, a_column) {
	td = data.frame()
	td = mergeColumn(td, a_dataSet1, a_column, "d1", "group")
	td = mergeColumn(td, a_dataSet2, a_column, "d2", "group")

	ret = list()
	ret$p = pvalue(wilcox_test(td[[a_column]]~td$group))
	ret$Z = statistic(wilcox_test(td[[a_column]]~td$group))
	ret$r = abs(ret$Z) / sqrt(nrow(td))
	ret$median1 = median(a_dataSet1[[a_column]])
	ret$median2 = median(a_dataSet2[[a_column]])
	ret$medianDiff = ret$median1 - ret$median2

	ret
}



wilcoxTest(nbc_jabref, hugme_jabref$data, "performance")
wilcoxTest(nbc_jabref, hugme_manual_jabref$data, "performance")

wilcoxTest(nbc_ant, hugme_ant$data, "performance")
wilcoxTest(nbc_ant, hugme_manual_ant$data, "performance")

wilcoxTest(nbc_argouml, hugme_argouml$data, "performance")
wilcoxTest(nbc_argouml, hugme_manual_argouml$data, "performance")

wilcoxTest(nbc_lucene, hugme_lucene$data, "performance")
wilcoxTest(nbc_lucene, hugme_manual_lucene$data, "performance")

wilcoxTest(nbc_teammates, hugme_teammates$data, "performance")
wilcoxTest(nbc_teammates, hugme_manual_teammates$data, "performance")

wilcoxTest(nbc_sweethome3d, hugme_sweethome3d$data, "performance")
wilcoxTest(nbc_sweethome3d, hugme_manual_sweethome3d$data, "performance")


wilcoxTest(nbc_jabref, hugme_jabref$data, "f1")
wilcoxTest(nbc_jabref, hugme_manual_jabref$data, "f1")

wilcoxTest(nbc_ant, hugme_ant$data, "f1")
wilcoxTest(nbc_ant, hugme_manual_ant$data, "f1")

wilcoxTest(nbc_argouml, hugme_argouml$data, "f1")
wilcoxTest(nbc_argouml, hugme_manual_argouml$data, "f1")

wilcoxTest(nbc_lucene, hugme_lucene$data, "f1")
wilcoxTest(nbc_lucene, hugme_manual_lucene$data, "f1")

wilcoxTest(nbc_teammates, hugme_teammates$data, "f1")
wilcoxTest(nbc_teammates, hugme_manual_teammates$data, "f1")

wilcoxTest(nbc_sweethome3d, hugme_sweethome3d$data, "f1")
wilcoxTest(nbc_sweethome3d, hugme_manual_sweethome3d$data, "f1")




# fun and tests
hist(nbc_jabref$recall)

summary(nbc_jabref)

# this is to compare with the large evo changes of Improving Automated Mapping in Reflexion Models Using Information Retrieval Techniques
getMeanF1LargeEvoChange <- function(a_data) {
	mean(a_data[a_data$initialClustered > (a_data$totalMapped - 100) & a_data$initialClustered < (a_data$totalMapped - 10),]$f1)
}

getMeanF1LargeEvoChange(nbc_jabref)
getMeanF1LargeEvoChange(nbc_ant)
getMeanF1LargeEvoChange(nbc_argouml)
getMeanF1LargeEvoChange(nbc_lucene)
getMeanF1LargeEvoChange(nbc_teammates)
getMeanF1LargeEvoChange(nbc_sweethome3d)

