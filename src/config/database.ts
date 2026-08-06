// src/config/database.ts
import { Pool } from 'pg';

export const pool = new Pool({
  connectionString: 'postgresql://raizes_app:secret_password@localhost:5433/raizes_db'
});