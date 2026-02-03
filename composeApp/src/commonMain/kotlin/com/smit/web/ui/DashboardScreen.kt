package com.smit.web.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smit.web.*
import com.smit.web.ui.components.DeploymentCard
import kotlinx.coroutines.launch

import com.smit.web.ui.components.SettingsDialog
import com.smit.web.ui.components.ProfileDialog

@Composable
fun DashboardScreen() {
    val apiService = remember { ApiService() }
    val webSocketService = remember { WebSocketService() }
    
    // Use mutableStateListOf for more reliable list updates in Compose
    val deployments = remember { mutableStateListOf<Deployment>() }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<DeploymentStatus?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var redeployingTemplate by remember { mutableStateOf<Deployment?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var viewingLogsFor by remember { mutableStateOf<Deployment?>(null) }
    var showUserMenu by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    val currentUser by AuthStore.username.collectAsState()

    // Filtered list derived from the state list - reactive to individual element changes
    val filteredDeployments by remember {
        derivedStateOf {
            deployments.filter {
                (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)) &&
                (selectedFilter == null || it.status == selectedFilter)
            }.sortedByDescending { it.id }
        }
    }

    // Stats derived from the state list
    val stats by remember {
        derivedStateOf {
            mapOf(
                "Total" to deployments.size,
                "Active" to deployments.count { it.status == DeploymentStatus.IN_PROGRESS || it.status == DeploymentStatus.PENDING },
                "Failed" to deployments.count { it.status == DeploymentStatus.FAILED },
                "Done" to deployments.count { it.status == DeploymentStatus.COMPLETED }
            )
        }
    }

    // LaunchedEffect for initial data fetching
    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        try {
            val initial = apiService.getDeployments()
            deployments.clear()
            deployments.addAll(initial)
        } catch (e: Exception) {
            error = "Failed to fetch deployments: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // LaunchedEffect for WebSocket connection and updates
    LaunchedEffect(Unit) {
        launch {
            while (true) {
                try {
                    webSocketService.connectAndObserveDeployments()
                } catch (e: Exception) {
                    println("WebSocket connection error: ${e.message}. Retrying in 5 seconds...")
                    kotlinx.coroutines.delay(5000)
                }
            }
        }

        launch {
            webSocketService.deployments.collect { newDeployment ->
                val currentList = deployments.toList()
                val index = currentList.indexOfFirst { it.id == newDeployment.id }
                
                val updatedList = if (index != -1) {
                    currentList.map { if (it.id == newDeployment.id) newDeployment else it }
                } else {
                    listOf(newDeployment) + currentList
                }
                
                deployments.clear()
                deployments.addAll(updatedList)
            }
        }

        launch {
            webSocketService.deploymentDeletions.collect { deletedId ->
                val updatedList = deployments.filter { it.id != deletedId }
                deployments.clear()
                deployments.addAll(updatedList)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(Icons.Default.Add, "Create Deployment", modifier = Modifier.size(32.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Header & Stats
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DevOps Dashboard",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (AuthStore.isAdmin()) {
                            IconButton(onClick = { showSettingsDialog = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                        
                        Box {
                            IconButton(onClick = { showUserMenu = true }) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "User Profile", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            }
                            DropdownMenu(
                                expanded = showUserMenu,
                                onDismissRequest = { showUserMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Profile: $currentUser") },
                                    leadingIcon = { Icon(Icons.Default.Person, null) },
                                    onClick = { 
                                        showUserMenu = false 
                                        showProfileDialog = true
                                    }
                                )
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    leadingIcon = { Icon(Icons.Default.ExitToApp, null) },
                                    onClick = { 
                                        showUserMenu = false
                                        AuthStore.clearSession()
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    stats.forEach { (label, count) ->
                        StatCard(label, count.toString(), modifier = Modifier.weight(1f))
                    }
                }
            }

            // Search and Filters
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search deployments...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                SecondaryScrollableTabRow(
                    selectedTabIndex = if (selectedFilter == null) 0 else DeploymentStatus.values().indexOf(selectedFilter) + 1,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = {}
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All") },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    DeploymentStatus.values().forEach { status ->
                        FilterChip(
                            selected = selectedFilter == status,
                            onClick = { selectedFilter = status },
                            label = { Text(status.name) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error)
                }
            } else if (filteredDeployments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No matching deployments.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDeployments, key = { it.id }) { deployment ->
                        DeploymentCard(
                            deployment = deployment,
                            onDeleteClick = { id ->
                                coroutineScope.launch {
                                    try {
                                        apiService.deleteDeployment(id)
                                        snackbarHostState.showSnackbar("Deployment deleted")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Failed to delete")
                                    }
                                }
                            },
                            onStatusUpdateClick = { id, status ->
                                coroutineScope.launch {
                                    try {
                                        apiService.updateDeploymentStatus(id, status)
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Failed to update status")
                                    }
                                }
                            },
                            onLogsClick = { viewingLogsFor = it },
                            onRedeployClick = { 
                                redeployingTemplate = it
                                showCreateDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateDeploymentDialog(
            templateDeployment = redeployingTemplate,
            onDismiss = { 
                showCreateDialog = false
                redeployingTemplate = null
            },
            onConfirm = { name, description, repositoryUrl, branch, serviceUrl ->
                coroutineScope.launch {
                    try {
                        apiService.createDeployment(name, description, repositoryUrl, branch, serviceUrl)
                        showCreateDialog = false
                        redeployingTemplate = null
                        snackbarHostState.showSnackbar("Deployment created")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Failed to create: ${e.message}")
                    }
                }
            }
        )
    }

    viewingLogsFor?.let { deployment ->
        LogViewerDialog(
            deployment = deployment,
            onDismiss = { viewingLogsFor = null }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false }
        )
    }
    
    if (showProfileDialog) {
        ProfileDialog(
            onDismiss = { showProfileDialog = false }
        )
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}
