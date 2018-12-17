analyzeSystem <- function(a_best_percent, a_data, min, max) {
	
	rand_loaded <- a_data[a_data$metric == "rand",];
#	rand_omegaphi <- getOmegaPhi(a_best_percent, a_data)
	rand_omegaphi <- getOmegaPhiInRange(a_best_percent, a_data, min, max)
	print(rand_omegaphi$omega_max - rand_omegaphi$omega_min)
	print(rand_omegaphi$phi_max - rand_omegaphi$phi_min)
	all_filtered <- getFilteredData_OmegaPhi(min, max, rand_omegaphi, a_data)
	all_filtered$metrics <- with(all_filtered$data, reorder(metric, -h_mam, FUN=median))
#	oldpar = par(mar=c(20,5,2,1))
#	boxplot(h_mam~all_filtered$metrics, data=all_filtered$data, las=2)
#	title(a_data$system[1])

	if (nrow(all_filtered$data) < 1) {
		message(paste(toString(a_data$system[1]), "No Data min", min, "max", max))
		return (all_filtered)
	}

	#medians <- aggregate(all_filtered$data$h_mam~all_filtered$metrics, FUN=median)

	#colnames(medians) <- c("metric", "h_mam")
	#rand <- medians[medians$metric == "rand",2]

	#rows = c()
	#rows
	#rownames = c()
	#for(i in 1:nrow(medians)) {
	#	row = c(medians[i,2], medians[i,2] - rand, (medians[i,2] - rand) / rand)
	#	rows = append(rows, row)
	#	name = toString(medians[i,1])
	#	rownames = append(rownames, c(name))
	#	#message(name, medians[i,2], medians[i,2] - rand, (medians[i,2] - rand) / rand )
	#}

	#table = matrix(rows, ncol=3, byrow=TRUE)
	#system = toString(a_data$system[1])
	#colnames(table) <- c(paste(system, "median", sep="_"), paste(system, "median_diff", sep="_"), paste(system, "median_diff_p", sep="_"))
	#rownames(table) <- rownames


	#all_filtered$table <- table

	return(all_filtered)
}


# merges the a_t2 as new columns in a_t1 matching the rownames
mergeTablesByRow <- function(a_t1, a_t2) {
	ret <- cbind(a_t1, a_t2[match(rownames(a_t1), rownames(a_t2)),])
	return(ret)
}

printOmegaPhi <- function(a_filteredData) {
	message(a_filteredData$omega_min)
	message(a_filteredData$omega_max)
	message(a_filteredData$phi_min)
	message(a_filteredData$phi_max)
}

# renames metrics attribute from S4RdM3X names to short names
renameFactor <- function(a_data, a_from, a_to) {
	levels(a_data)[levels(a_data) == a_from] <- a_to
	a_data[a_data == a_from] <- a_to
	return(a_data)
}

renameMetrics <- function(a_data) {

	namePairs <- c(	"linecount", "lc",
				"NumberOfChildren", "noc",
				"NumberOfParents", "nop",
				"ByteCodeInstructions", "bci",
				"ByteCodeCyclomaticComplexity", "bccc",
				"CouplingOut", "cout",
				"CouplingIn", "cin",
				"fanout", "fout",
				"fanin", "fin",
				"Rank", "rank",
				"NumberOfMethods", "nom",
				"NumberOfFields", "nof",
				"LackOfCohesionOfMethods_HS", "lcom",

				"NumberOfChildren_linecount", "rel noc",
				"NumberOfParents_linecount", "rel nop",
				"ByteCodeInstructions_linecount", "rel bci",
				"ByteCodeCyclomaticComplexity_linecount", "rel bccc",
				"CouplingOut_linecount", "rel cout",
				"CouplingIn_linecount", "rel cin",
				"fanout_linecount", "rel fout",
				"fanin_linecount", "rel fin",
				"Rank_linecount", "rel rank",
				"NumberOfMethods_linecount", "rel nom",
				"NumberOfFields_linecount", "nof",
				"LackOfCohesionOfMethods_HS_linecount", "rel lcom")

	for (i in 1:(length(namePairs)/2)) {
		a_data$metric <- renameFactor(a_data$metric, namePairs[(i - 1) * 2 + 1], namePairs[(i - 1) * 2 + 2])

	}


	return(a_data)
}

# library("ineq")
initialDistributionToGini <- function(a_distribution) {
	str <- as.character(a_distribution)
	str <- substr(str, 2, nchar(str) - 1)
	parts <- strsplit(str, ",")

	number_of_concrete_elements = c()
	for (i in 1: length(parts[[1]])) {
		n <- as.numeric(strsplit(parts[[1]][i], ":")[[1]][2])
		number_of_concrete_elements = append(number_of_concrete_elements,n)
	}

	return(ineq(number_of_concrete_elements, type="Gini"))

}


# returns a random sample of a metric with an even number of samples for each system
randomSample <- function(a_selectedData) {
	ret <- do.call(rbind, 
        lapply(split(a_selectedData, a_selectedData$system), 
               function(x) x[sample(nrow(x), min(summary(a_selectedData$system))), ]))
	return(ret)
}
